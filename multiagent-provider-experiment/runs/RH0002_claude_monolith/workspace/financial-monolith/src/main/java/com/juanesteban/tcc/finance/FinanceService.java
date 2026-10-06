package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class FinanceService {

    static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00"));

    private final UserRepository users;
    private final PositionRepository positions;
    private final TradeRepository trades;

    FinanceService(UserRepository users, PositionRepository positions, TradeRepository trades) {
        this.users = users;
        this.positions = positions;
        this.trades = trades;
    }

    static ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }

    static BigDecimal price(String symbol) {
        BigDecimal p = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (p == null) throw bad("Unknown symbol");
        return p;
    }

    @Transactional
    AppUser createUser(String username) {
        if (username == null || username.isBlank()) throw bad("username required");
        if (users.existsByUsername(username)) throw bad("username already exists");
        AppUser u = new AppUser();
        u.id = UUID.randomUUID();
        u.username = username;
        u.cash = new BigDecimal("10000.00");
        try {
            return users.saveAndFlush(u);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw bad("username already exists");
        }
    }

    @Transactional
    Trade trade(UUID userId, String symbol, long shares, boolean buy) {
        if (userId == null) throw bad("userId required");
        if (shares <= 0) throw bad("shares must be positive");
        BigDecimal price = price(symbol);
        String sym = symbol.trim().toUpperCase();
        AppUser u = users.findForUpdate(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Position pos = positions.findByUserIdAndSymbol(userId, sym).orElse(null);
        if (buy) {
            if (u.cash.compareTo(total) < 0) throw bad("insufficient funds");
            u.cash = u.cash.subtract(total);
            if (pos == null) {
                pos = new Position();
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
        if (pos.shares == 0) positions.delete(pos); else positions.save(pos);
        Trade t = new Trade();
        t.userId = userId;
        t.type = buy ? "BUY" : "SELL";
        t.symbol = sym;
        t.shares = shares;
        t.price = price;
        t.total = total;
        t.createdAt = Instant.now();
        return trades.save(t);
    }

    AppUser requireUser(UUID id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    @Transactional(readOnly = true)
    Map<String, Object> portfolio(UUID id) {
        AppUser u = requireUser(id);
        BigDecimal total = u.cash;
        List<Map<String, Object>> holdings = new ArrayList<>();
        for (Position p : positions.findByUserIdOrderBySymbol(id)) {
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
        r.put("username", u.username);
        r.put("cash", u.cash);
        r.put("cashBalance", u.cash);
        r.put("holdings", holdings);
        r.put("totalValue", total);
        return r;
    }

    @Transactional(readOnly = true)
    List<Map<String, Object>> history(UUID id) {
        requireUser(id);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Trade t : trades.findByUserIdOrderByIdAsc(id)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.id);
            m.put("userId", t.userId);
            m.put("type", t.type);
            m.put("symbol", t.symbol);
            m.put("shares", t.shares);
            m.put("price", t.price);
            m.put("total", t.total);
            m.put("createdAt", t.createdAt);
            out.add(m);
        }
        return out;
    }
}
