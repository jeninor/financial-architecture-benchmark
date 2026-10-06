package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FinanceService {
    private static final Map<String, BigDecimal> PRICES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    private final UserRepository users;
    private final HoldingRepository holdings;
    private final TradeRepository trades;

    public FinanceService(UserRepository users, HoldingRepository holdings, TradeRepository trades) {
        this.users = users;
        this.holdings = holdings;
        this.trades = trades;
    }

    public BigDecimal price(String symbol) {
        BigDecimal price = PRICES.get(symbol);
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symbol");
        }
        return price;
    }

    @Transactional
    public UserAccount createUser(String username) {
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        return users.saveAndFlush(new UserAccount(username));
    }

    @Transactional
    public Trade buy(UUID userId, String symbol, int shares) {
        BigDecimal price = price(symbol);
        UserAccount user = lockedUser(userId);
        BigDecimal amount = price.multiply(BigDecimal.valueOf(shares));
        if (user.getCashBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient cash");
        }

        Holding holding = holdings.findByUserIdAndSymbol(userId, symbol).orElse(null);
        if (holding == null) {
            holdings.save(new Holding(user, symbol, shares));
        } else {
            if (holding.getShares() > Integer.MAX_VALUE - shares) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many shares");
            }
            holding.setShares(holding.getShares() + shares);
        }
        user.setCashBalance(user.getCashBalance().subtract(amount));
        return trades.save(new Trade(user, "BUY", symbol, shares, price));
    }

    @Transactional
    public Trade sell(UUID userId, String symbol, int shares) {
        BigDecimal price = price(symbol);
        UserAccount user = lockedUser(userId);
        Holding holding = holdings.findByUserIdAndSymbol(userId, symbol).orElse(null);
        if (holding == null || holding.getShares() < shares) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient position");
        }

        if (holding.getShares() == shares) {
            holdings.delete(holding);
        } else {
            holding.setShares(holding.getShares() - shares);
        }
        user.setCashBalance(user.getCashBalance().add(price.multiply(BigDecimal.valueOf(shares))));
        return trades.save(new Trade(user, "SELL", symbol, shares, price));
    }

    @Transactional(readOnly = true)
    public Portfolio portfolio(UUID userId) {
        UserAccount user = user(userId);
        List<PortfolioHolding> positions = holdings.findByUserIdOrderBySymbol(userId).stream()
            .map(holding -> {
                BigDecimal price = price(holding.getSymbol());
                return new PortfolioHolding(holding.getSymbol(), holding.getShares(), price,
                    price.multiply(BigDecimal.valueOf(holding.getShares())));
            })
            .toList();
        BigDecimal total = positions.stream()
            .map(PortfolioHolding::marketValue)
            .reduce(user.getCashBalance(), BigDecimal::add);
        return new Portfolio(userId, user.getCashBalance(), positions, total);
    }

    @Transactional(readOnly = true)
    public List<TradeView> history(UUID userId) {
        user(userId);
        return trades.findByUserIdOrderByCreatedAtAsc(userId).stream()
            .map(trade -> new TradeView(trade.getId(), trade.getType(), trade.getSymbol(),
                trade.getShares(), trade.getPrice(), trade.getCreatedAt()))
            .toList();
    }

    private UserAccount user(UUID userId) {
        return users.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserAccount lockedUser(UUID userId) {
        return users.findByIdForUpdate(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public record Portfolio(UUID userId, BigDecimal cashBalance,
                            List<PortfolioHolding> holdings, BigDecimal totalValue) {}

    public record PortfolioHolding(String symbol, int shares, BigDecimal price,
                                   BigDecimal marketValue) {}

    public record TradeView(UUID id, String type, String symbol, int shares,
                            BigDecimal price, java.time.Instant createdAt) {}
}
