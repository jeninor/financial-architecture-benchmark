package com.juanesteban.tcc.finance;

import com.juanesteban.tcc.finance.Entities.*;
import com.juanesteban.tcc.finance.Repositories.*;
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

    private static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00"));

    private static final BigDecimal INITIAL_CASH = new BigDecimal("10000.00");

    private final UserRepository users;
    private final PositionRepository positions;
    private final TradeRepository trades;

    public FinanceService(UserRepository users, PositionRepository positions, TradeRepository trades) {
        this.users = users;
        this.positions = positions;
        this.trades = trades;
    }

    public BigDecimal quote(String symbol) {
        BigDecimal price = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (price == null) {
            throw bad("Unknown symbol");
        }
        return price;
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    @Transactional
    public AppUser createUser(String username) {
        if (username == null || username.isBlank()) {
            throw bad("username required");
        }
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw bad("username already exists");
        }
        AppUser u = new AppUser();
        u.id = UUID.randomUUID();
        u.username = name;
        u.cashBalance = INITIAL_CASH;
        try {
            return users.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw bad("username already exists");
        }
    }

    public AppUser getUser(UUID id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public Trade trade(UUID userId, String symbol, Long shares, boolean buy) {
        if (userId == null) {
            throw bad("userId required");
        }
        if (shares == null || shares <= 0) {
            throw bad("shares must be positive");
        }
        BigDecimal price = quote(symbol);
        String sym = symbol.trim().toUpperCase();
        AppUser user = users.findForUpdate(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Position pos = positions.findByUserIdAndSymbol(userId, sym).orElse(null);

        if (buy) {
            if (user.cashBalance.compareTo(total) < 0) {
                throw bad("insufficient funds");
            }
            if (pos == null) {
                pos = new Position();
                pos.id = UUID.randomUUID();
                pos.userId = userId;
                pos.symbol = sym;
            }
            pos.shares += shares;
            user.cashBalance = user.cashBalance.subtract(total);
        } else {
            if (pos == null || pos.shares < shares) {
                throw bad("insufficient position");
            }
            pos.shares -= shares;
            user.cashBalance = user.cashBalance.add(total);
        }
        positions.save(pos);
        users.save(user);

        Trade t = new Trade();
        t.id = UUID.randomUUID();
        t.userId = userId;
        t.symbol = sym;
        t.type = buy ? "BUY" : "SELL";
        t.shares = shares;
        t.price = price;
        t.createdAt = Instant.now();
        return trades.save(t);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> portfolio(UUID userId) {
        AppUser user = getUser(userId);
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = user.cashBalance;
        for (Position p : positions.findByUserIdOrderBySymbol(userId)) {
            if (p.shares <= 0) {
                continue;
            }
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
        r.put("userId", user.id);
        r.put("username", user.username);
        r.put("cash", user.cashBalance);
        r.put("cashBalance", user.cashBalance);
        r.put("holdings", holdings);
        r.put("totalValue", total);
        return r;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> history(UUID userId) {
        getUser(userId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Trade t : trades.findByUserIdOrderByCreatedAtAsc(userId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.id);
            m.put("userId", t.userId);
            m.put("symbol", t.symbol);
            m.put("type", t.type);
            m.put("shares", t.shares);
            m.put("price", t.price);
            m.put("total", t.price.multiply(BigDecimal.valueOf(t.shares)));
            m.put("createdAt", t.createdAt);
            out.add(m);
        }
        return out;
    }
}
