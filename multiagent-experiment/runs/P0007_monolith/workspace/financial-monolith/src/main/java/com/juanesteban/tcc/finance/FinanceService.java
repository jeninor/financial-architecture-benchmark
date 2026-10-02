package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static ResponseStatusException status(HttpStatus s, String msg) {
        return new ResponseStatusException(s, msg);
    }

    BigDecimal price(String symbol) {
        BigDecimal p = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (p == null) throw status(HttpStatus.BAD_REQUEST, "unknown symbol");
        return p;
    }

    @Transactional
    AppUser createUser(String username) {
        if (username == null || username.isBlank()) throw status(HttpStatus.BAD_REQUEST, "username required");
        if (users.existsByUsername(username)) throw status(HttpStatus.BAD_REQUEST, "username already exists");
        AppUser u = new AppUser();
        u.username = username;
        u.cash = new BigDecimal("10000.00");
        try {
            return users.saveAndFlush(u);
        } catch (DataIntegrityViolationException e) {
            throw status(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    @Transactional
    TradeRecord trade(boolean buy, UUID userId, String symbol, Integer shares) {
        if (userId == null || shares == null || shares <= 0) throw status(HttpStatus.BAD_REQUEST, "invalid request");
        BigDecimal price = price(symbol);
        String sym = symbol.trim().toUpperCase();
        AppUser u = users.findForUpdate(userId).orElseThrow(() -> status(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Position pos = positions.findByUserIdAndSymbol(userId, sym).orElse(null);
        if (buy) {
            if (u.cash.compareTo(total) < 0) throw status(HttpStatus.BAD_REQUEST, "insufficient funds");
            u.cash = u.cash.subtract(total);
            if (pos == null) {
                pos = new Position();
                pos.userId = userId;
                pos.symbol = sym;
            }
            pos.shares += shares;
            positions.save(pos);
        } else {
            if (pos == null || pos.shares < shares) throw status(HttpStatus.BAD_REQUEST, "insufficient position");
            u.cash = u.cash.add(total);
            pos.shares -= shares;
            if (pos.shares == 0) positions.delete(pos);
            else positions.save(pos);
        }
        users.save(u);
        TradeRecord t = new TradeRecord();
        t.userId = userId;
        t.type = buy ? "BUY" : "SELL";
        t.symbol = sym;
        t.shares = shares;
        t.price = price;
        t.total = total;
        return trades.save(t);
    }

    @Transactional(readOnly = true)
    Map<String, Object> portfolio(UUID userId) {
        AppUser u = users.findById(userId).orElseThrow(() -> status(HttpStatus.NOT_FOUND, "user not found"));
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = u.cash;
        for (Position p : positions.findByUserIdOrderBySymbol(userId)) {
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
    List<Map<String, Object>> history(UUID userId) {
        if (!users.existsById(userId)) throw status(HttpStatus.NOT_FOUND, "user not found");
        List<Map<String, Object>> out = new ArrayList<>();
        for (TradeRecord t : trades.findByUserIdOrderByIdAsc(userId)) out.add(view(t));
        return out;
    }

    static Map<String, Object> view(TradeRecord t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("userId", t.userId);
        m.put("type", t.type);
        m.put("symbol", t.symbol);
        m.put("shares", t.shares);
        m.put("price", t.price);
        m.put("total", t.total);
        m.put("createdAt", t.createdAt);
        return m;
    }
}
