package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.AppUser;
import com.juanesteban.tcc.finance.domain.Position;
import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.repo.AppUserRepository;
import com.juanesteban.tcc.finance.repo.PositionRepository;
import com.juanesteban.tcc.finance.repo.TradeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradingService {

    private static final BigDecimal INITIAL_CASH = new BigDecimal("10000.00");

    private final AppUserRepository users;
    private final PositionRepository positions;
    private final TradeRepository trades;
    private final QuoteService quotes;

    public TradingService(AppUserRepository users, PositionRepository positions,
                          TradeRepository trades, QuoteService quotes) {
        this.users = users;
        this.positions = positions;
        this.trades = trades;
        this.quotes = quotes;
    }

    public AppUser createUser(String username) {
        if (username == null || username.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username is required");
        }
        if (users.existsByUsername(username)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        try {
            return users.saveAndFlush(new AppUser(username, INITIAL_CASH));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    @Transactional
    public Trade buy(UUID userId, String symbol, Long shares) {
        BigDecimal price = validate(symbol, shares);
        AppUser user = lockUser(userId);
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        if (user.getCash().compareTo(total) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "insufficient funds");
        }
        user.setCash(user.getCash().subtract(total));
        Position p = positions.findByUserIdAndSymbol(userId, symbol)
                .orElseGet(() -> new Position(userId, symbol, 0));
        p.setShares(p.getShares() + shares);
        positions.save(p);
        return trades.save(new Trade(userId, symbol, "BUY", shares, price, total));
    }

    @Transactional
    public Trade sell(UUID userId, String symbol, Long shares) {
        BigDecimal price = validate(symbol, shares);
        AppUser user = lockUser(userId);
        Position p = positions.findByUserIdAndSymbol(userId, symbol).orElse(null);
        if (p == null || p.getShares() < shares) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "insufficient position");
        }
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));
        user.setCash(user.getCash().add(total));
        p.setShares(p.getShares() - shares);
        positions.save(p);
        return trades.save(new Trade(userId, symbol, "SELL", shares, price, total));
    }

    @Transactional(readOnly = true)
    public AppUser getUser(UUID userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
    }

    @Transactional(readOnly = true)
    public List<Position> holdings(UUID userId) {
        return positions.findByUserIdOrderBySymbol(userId).stream()
                .filter(p -> p.getShares() > 0).toList();
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        getUser(userId);
        return trades.findByUserIdOrderByCreatedAtAsc(userId);
    }

    private BigDecimal validate(String symbol, Long shares) {
        if (shares == null || shares <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "shares must be positive");
        }
        return quotes.priceOf(symbol);
    }

    private AppUser lockUser(UUID userId) {
        return users.findForUpdate(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "user not found"));
    }
}
