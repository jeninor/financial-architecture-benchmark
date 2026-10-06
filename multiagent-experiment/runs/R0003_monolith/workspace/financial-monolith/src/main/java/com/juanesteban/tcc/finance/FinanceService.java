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

    private static final Map<String, BigDecimal> QUOTES = Map.of(
            "AAPL", new BigDecimal("200.00"),
            "MSFT", new BigDecimal("400.00"),
            "GOOGL", new BigDecimal("170.00"),
            "AMZN", new BigDecimal("190.00"),
            "NVDA", new BigDecimal("120.00"));

    private final UserRepository users;
    private final HoldingRepository holdings;
    private final TradeRepository trades;

    public FinanceService(UserRepository users, HoldingRepository holdings, TradeRepository trades) {
        this.users = users;
        this.holdings = holdings;
        this.trades = trades;
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
    }

    public BigDecimal price(String symbol) {
        BigDecimal p = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (p == null) {
            throw bad("unknown symbol");
        }
        return p;
    }

    private static String sym(String s) {
        return s.trim().toUpperCase();
    }

    @Transactional
    public AppUser createUser(String username) {
        if (username == null || username.isBlank()) {
            throw bad("username required");
        }
        if (users.existsByUsername(username)) {
            throw bad("username already exists");
        }
        AppUser u = new AppUser();
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
    public Trade trade(String type, UUID userId, String symbol, Integer shares) {
        if (userId == null || symbol == null || shares == null || shares <= 0) {
            throw bad("invalid request");
        }
        BigDecimal price = price(symbol);
        String s = sym(symbol);
        AppUser u = users.findForUpdate(userId).orElseThrow(FinanceService::notFound);
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Optional<Holding> existing = holdings.findByUserIdAndSymbol(userId, s);
        if (type.equals("BUY")) {
            if (u.cash.compareTo(total) < 0) {
                throw bad("insufficient funds");
            }
            u.cash = u.cash.subtract(total);
            Holding h = existing.orElseGet(() -> {
                Holding n = new Holding();
                n.id = UUID.randomUUID();
                n.userId = userId;
                n.symbol = s;
                return n;
            });
            h.shares += shares;
            holdings.save(h);
        } else {
            Holding h = existing.orElseThrow(() -> bad("insufficient position"));
            if (h.shares < shares) {
                throw bad("insufficient position");
            }
            u.cash = u.cash.add(total);
            h.shares -= shares;
            if (h.shares == 0) {
                holdings.delete(h);
            } else {
                holdings.save(h);
            }
        }
        users.save(u);
        Trade t = new Trade();
        t.id = UUID.randomUUID();
        t.userId = userId;
        t.type = type;
        t.symbol = s;
        t.shares = shares;
        t.price = price;
        t.total = total;
        t.createdAt = Instant.now();
        return trades.save(t);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> portfolio(UUID userId) {
        AppUser u = users.findById(userId).orElseThrow(FinanceService::notFound);
        List<Map<String, Object>> list = new ArrayList<>();
        BigDecimal total = u.cash;
        for (Holding h : holdings.findByUserIdOrderBySymbol(userId)) {
            BigDecimal p = QUOTES.get(h.symbol);
            BigDecimal mv = p.multiply(BigDecimal.valueOf(h.shares));
            total = total.add(mv);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("symbol", h.symbol);
            m.put("shares", h.shares);
            m.put("price", p);
            m.put("marketValue", mv);
            list.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", u.id);
        out.put("username", u.username);
        out.put("cash", u.cash);
        out.put("cashBalance", u.cash);
        out.put("holdings", list);
        out.put("totalValue", total);
        return out;
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        if (!users.existsById(userId)) {
            throw notFound();
        }
        return trades.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
