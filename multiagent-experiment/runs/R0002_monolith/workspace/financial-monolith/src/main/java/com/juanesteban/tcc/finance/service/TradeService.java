package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.Position;
import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.domain.TradeType;
import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.repository.PositionRepository;
import com.juanesteban.tcc.finance.repository.TradeRepository;
import com.juanesteban.tcc.finance.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {

    private final UserRepository users;
    private final PositionRepository positions;
    private final TradeRepository trades;
    private final QuoteService quotes;

    public TradeService(UserRepository users, PositionRepository positions,
                        TradeRepository trades, QuoteService quotes) {
        this.users = users;
        this.positions = positions;
        this.trades = trades;
        this.quotes = quotes;
    }

    @Transactional
    public Trade buy(UUID userId, String symbol, long shares) {
        return execute(TradeType.BUY, userId, symbol, shares);
    }

    @Transactional
    public Trade sell(UUID userId, String symbol, long shares) {
        return execute(TradeType.SELL, userId, symbol, shares);
    }

    private Trade execute(TradeType type, UUID userId, String rawSymbol, long shares) {
        if (shares <= 0) {
            throw new BadRequestException("shares must be positive");
        }
        BigDecimal price = quotes.priceOf(rawSymbol);
        String symbol = quotes.normalize(rawSymbol);
        User user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        BigDecimal total = price.multiply(BigDecimal.valueOf(shares));

        if (type == TradeType.BUY) {
            if (user.getCashBalance().compareTo(total) < 0) {
                throw new BadRequestException("Insufficient balance");
            }
            user.setCashBalance(user.getCashBalance().subtract(total));
            Position position = positions.findByUserIdAndSymbol(userId, symbol)
                    .orElseGet(() -> new Position(userId, symbol, 0));
            position.setShares(position.getShares() + shares);
            positions.save(position);
        } else {
            Position position = positions.findByUserIdAndSymbol(userId, symbol)
                    .filter(p -> p.getShares() >= shares)
                    .orElseThrow(() -> new BadRequestException("Insufficient position"));
            position.setShares(position.getShares() - shares);
            positions.save(position);
            user.setCashBalance(user.getCashBalance().add(total));
        }
        users.save(user);
        return trades.save(new Trade(userId, symbol, type, shares, price, total));
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        if (!users.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        return trades.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
