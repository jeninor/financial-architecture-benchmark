package com.juanesteban.tcc.finance.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    private final AppUserRepository users;
    private final HoldingRepository holdings;
    private final TradeRepository trades;
    private final QuoteService quotes;

    public FinanceService(AppUserRepository users, HoldingRepository holdings,
                          TradeRepository trades, QuoteService quotes) {
        this.users = users;
        this.holdings = holdings;
        this.trades = trades;
        this.quotes = quotes;
    }

    public AppUser createUser(String username) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("username is required");
        }
        if (users.existsByUsername(username)) {
            throw new BadRequestException("username already exists");
        }
        try {
            return users.saveAndFlush(new AppUser(username));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("username already exists");
        }
    }

    @Transactional
    public Trade buy(UUID userId, String symbol, Long shares) {
        validate(symbol, shares);
        BigDecimal price = quotes.priceOf(symbol);
        AppUser user = lockUser(userId);
        BigDecimal cost = price.multiply(BigDecimal.valueOf(shares));
        if (user.getCashBalance().compareTo(cost) < 0) {
            throw new BadRequestException("insufficient funds");
        }
        user.setCashBalance(user.getCashBalance().subtract(cost));
        Holding holding = holdings.findByUserIdAndSymbol(userId, symbol)
                .orElseGet(() -> new Holding(userId, symbol, 0));
        holding.setShares(holding.getShares() + shares);
        holdings.save(holding);
        return trades.save(new Trade(userId, symbol, Trade.Type.BUY, shares, price));
    }

    @Transactional
    public Trade sell(UUID userId, String symbol, Long shares) {
        validate(symbol, shares);
        BigDecimal price = quotes.priceOf(symbol);
        AppUser user = lockUser(userId);
        Holding holding = holdings.findByUserIdAndSymbol(userId, symbol).orElse(null);
        if (holding == null || holding.getShares() < shares) {
            throw new BadRequestException("insufficient position");
        }
        user.setCashBalance(user.getCashBalance().add(price.multiply(BigDecimal.valueOf(shares))));
        holding.setShares(holding.getShares() - shares);
        holdings.save(holding);
        return trades.save(new Trade(userId, symbol, Trade.Type.SELL, shares, price));
    }

    @Transactional(readOnly = true)
    public Portfolio portfolio(UUID userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        List<HoldingView> views = holdings.findByUserIdAndSharesGreaterThanOrderBySymbol(userId, 0).stream()
                .map(h -> {
                    BigDecimal price = quotes.priceOf(h.getSymbol());
                    return new HoldingView(h.getSymbol(), h.getShares(), price,
                            price.multiply(BigDecimal.valueOf(h.getShares())));
                })
                .toList();
        BigDecimal total = views.stream().map(HoldingView::marketValue)
                .reduce(user.getCashBalance(), BigDecimal::add);
        return new Portfolio(user.getId(), user.getCashBalance(), user.getCashBalance(), views, total);
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        if (!users.existsById(userId)) {
            throw new NotFoundException("user not found");
        }
        return trades.findByUserIdOrderByCreatedAtAsc(userId);
    }

    private void validate(String symbol, Long shares) {
        if (shares == null || shares <= 0) {
            throw new BadRequestException("shares must be positive");
        }
        quotes.priceOf(symbol);
    }

    private AppUser lockUser(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("userId is required");
        }
        return users.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    public record HoldingView(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {
    }

    public record Portfolio(UUID userId, BigDecimal cash, BigDecimal cashBalance,
                            List<HoldingView> holdings, BigDecimal totalValue) {
    }
}
