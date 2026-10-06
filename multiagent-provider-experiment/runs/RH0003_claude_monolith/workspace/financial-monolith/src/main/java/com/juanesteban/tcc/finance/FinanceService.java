package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    private static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    private final UserRepository users;
    private final HoldingRepository holdings;
    private final TradeRepository trades;

    FinanceService(UserRepository users, HoldingRepository holdings, TradeRepository trades) {
        this.users = users;
        this.holdings = holdings;
        this.trades = trades;
    }

    public BigDecimal quote(String symbol) {
        BigDecimal price = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (price == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown symbol: " + symbol);
        }
        return price;
    }

    @Transactional
    public User createUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username is required");
        }
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        try {
            return users.saveAndFlush(new User(name));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    @Transactional
    public Trade trade(String type, UUID userId, String symbol, Integer shares) {
        if (userId == null || shares == null || shares <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid request");
        }
        BigDecimal price = quote(symbol);
        String sym = symbol.trim().toUpperCase();
        User user = users.findForUpdate(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Holding holding = holdings.findByUserIdAndSymbol(userId, sym).orElse(null);

        if (type.equals("BUY")) {
            if (user.getCashBalance().compareTo(total) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "insufficient funds");
            }
            user.setCashBalance(user.getCashBalance().subtract(total));
            if (holding == null) {
                holding = new Holding(userId, sym, 0);
            }
            holding.setShares(holding.getShares() + shares);
            holdings.save(holding);
        } else {
            if (holding == null || holding.getShares() < shares) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "insufficient position");
            }
            user.setCashBalance(user.getCashBalance().add(total));
            holding.setShares(holding.getShares() - shares);
            holdings.save(holding);
        }
        users.save(user);
        return trades.save(new Trade(userId, type, sym, shares, price));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> portfolio(UUID userId) {
        User user = users.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal total = user.getCashBalance();
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (Holding h : holdings.findByUserIdOrderBySymbol(userId)) {
            if (h.getShares() <= 0) {
                continue;
            }
            BigDecimal price = QUOTES.get(h.getSymbol());
            BigDecimal value = price.multiply(BigDecimal.valueOf(h.getShares()));
            total = total.add(value);
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("symbol", h.getSymbol());
            item.put("shares", h.getShares());
            item.put("price", price);
            item.put("marketValue", value);
            items.add(item);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("userId", userId);
        result.put("cash", user.getCashBalance());
        result.put("cashBalance", user.getCashBalance());
        result.put("holdings", items);
        result.put("totalValue", total);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        if (!users.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "user not found");
        }
        return trades.findByUserIdOrderByIdAsc(userId);
    }
}
