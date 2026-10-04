package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinanceService {

    static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00"));

    private final Repos.UserRepo users;
    private final Repos.PositionRepo positions;
    private final Repos.TradeRepo trades;

    public FinanceService(Repos.UserRepo users, Repos.PositionRepo positions, Repos.TradeRepo trades) {
        this.users = users;
        this.positions = positions;
        this.trades = trades;
    }

    static ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }

    BigDecimal price(String symbol) {
        BigDecimal p = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (p == null) throw bad("unknown symbol");
        return p;
    }

    @Transactional
    public Domain.AppUser createUser(String username) {
        if (username == null || username.isBlank()) throw bad("username required");
        if (users.existsByUsername(username)) throw bad("username already exists");
        Domain.AppUser u = new Domain.AppUser();
        u.id = UUID.randomUUID();
        u.username = username;
        u.cash = new BigDecimal("10000.00");
        try {
            return users.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw bad("username already exists");
        }
    }

    @Transactional
    public Domain.Trade trade(UUID userId, String symbol, Long shares, boolean buy) {
        if (userId == null) throw bad("userId required");
        if (shares == null || shares <= 0) throw bad("shares must be positive");
        BigDecimal price = price(symbol);
        String sym = symbol.trim().toUpperCase();
        Domain.AppUser u = users.lockById(userId).orElseThrow(this::notFound);
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Domain.Position pos = positions.findByUserIdAndSymbol(userId, sym).orElse(null);
        if (buy) {
            if (u.cash.compareTo(total) < 0) throw bad("insufficient funds");
            u.cash = u.cash.subtract(total);
            if (pos == null) {
                pos = new Domain.Position();
                pos.userId = userId;
                pos.symbol = sym;
            }
            pos.shares += shares;
        } else {
            if (pos == null || pos.shares < shares) throw bad("insufficient position");
            u.cash = u.cash.add(total);
            pos.shares -= shares;
        }
        users.save(u);
        positions.save(pos);
        Domain.Trade t = new Domain.Trade();
        t.userId = userId;
        t.symbol = sym;
        t.type = buy ? "BUY" : "SELL";
        t.shares = shares;
        t.price = price;
        t.total = total;
        t.createdAt = Instant.now();
        return trades.save(t);
    }

    ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> portfolio(UUID userId) {
        Domain.AppUser u = users.findById(userId).orElseThrow(this::notFound);
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = u.cash;
        for (Domain.Position p : positions.findByUserId(userId)) {
            if (p.shares <= 0) continue;
            BigDecimal price = QUOTES.get(p.symbol);
            BigDecimal mv = price.multiply(BigDecimal.valueOf(p.shares));
            total = total.add(mv);
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("symbol", p.symbol);
            h.put("shares", p.shares);
            h.put("price", price);
            h.put("marketValue", mv);
            holdings.add(h);
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("userId", u.id);
        r.put("cash", u.cash);
        r.put("cashBalance", u.cash);
        r.put("holdings", holdings);
        r.put("totalValue", total);
        return r;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> history(UUID userId) {
        if (!users.existsById(userId)) throw notFound();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Domain.Trade t : trades.findByUserIdOrderByIdAsc(userId)) out.add(view(t));
        return out;
    }

    static Map<String, Object> view(Domain.Trade t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("userId", t.userId);
        m.put("symbol", t.symbol);
        m.put("type", t.type);
        m.put("shares", t.shares);
        m.put("price", t.price);
        m.put("total", t.total);
        m.put("createdAt", t.createdAt);
        return m;
    }
}
