package com.juanesteban.tcc.finance.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String m) { super(m); }
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String m) { super(m); }
    }

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

    public BigDecimal quote(String symbol) {
        BigDecimal p = symbol == null ? null : QUOTES.get(symbol.trim().toUpperCase());
        if (p == null) throw new BadRequestException("Unknown symbol: " + symbol);
        return p;
    }

    @Transactional
    public AppUser createUser(String username) {
        if (username == null || username.isBlank()) throw new BadRequestException("username required");
        if (users.existsByUsername(username)) throw new BadRequestException("username already exists");
        try {
            return users.saveAndFlush(new AppUser(username, new BigDecimal("10000.00")));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("username already exists");
        }
    }

    @Transactional
    public Trade trade(Trade.Type type, UUID userId, String symbol, Integer shares) {
        if (userId == null) throw new BadRequestException("userId required");
        if (shares == null || shares <= 0) throw new BadRequestException("shares must be positive");
        BigDecimal price = quote(symbol);
        String sym = symbol.trim().toUpperCase();
        AppUser user = users.findForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        Holding h = holdings.findByUserIdAndSymbol(userId, sym).orElse(null);
        if (type == Trade.Type.BUY) {
            if (user.getCash().compareTo(total) < 0) throw new BadRequestException("insufficient funds");
            user.setCash(user.getCash().subtract(total));
            if (h == null) h = new Holding(userId, sym, shares);
            else h.setShares(h.getShares() + shares);
        } else {
            if (h == null || h.getShares() < shares) throw new BadRequestException("insufficient position");
            user.setCash(user.getCash().add(total));
            h.setShares(h.getShares() - shares);
        }
        users.save(user);
        holdings.save(h);
        return trades.save(new Trade(userId, type, sym, shares, price));
    }

    public record HoldingView(String symbol, int shares, BigDecimal price, BigDecimal marketValue) {}

    public record PortfolioView(UUID userId, BigDecimal cash, BigDecimal cashBalance,
                                List<HoldingView> holdings, BigDecimal holdingsValue, BigDecimal totalValue) {}

    @Transactional(readOnly = true)
    public PortfolioView portfolio(UUID userId) {
        AppUser u = users.findById(userId).orElseThrow(() -> new NotFoundException("user not found"));
        List<HoldingView> hv = holdings.findByUserIdOrderBySymbol(userId).stream()
                .filter(h -> h.getShares() > 0)
                .map(h -> {
                    BigDecimal p = QUOTES.get(h.getSymbol());
                    return new HoldingView(h.getSymbol(), h.getShares(), p, p.multiply(BigDecimal.valueOf(h.getShares())));
                }).toList();
        BigDecimal hvTotal = hv.stream().map(HoldingView::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2);
        return new PortfolioView(userId, u.getCash(), u.getCash(), hv, hvTotal, u.getCash().add(hvTotal));
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        if (!users.existsById(userId)) throw new NotFoundException("user not found");
        return trades.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
