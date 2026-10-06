package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.repository.PositionRepository;
import com.juanesteban.tcc.finance.repository.UserRepository;
import com.juanesteban.tcc.finance.web.Dtos.HoldingResponse;
import com.juanesteban.tcc.finance.web.Dtos.PortfolioResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final UserRepository users;
    private final PositionRepository positions;
    private final QuoteService quotes;

    public PortfolioService(UserRepository users, PositionRepository positions, QuoteService quotes) {
        this.users = users;
        this.positions = positions;
        this.quotes = quotes;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse get(UUID userId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        List<HoldingResponse> holdings = positions
                .findByUserIdAndSharesGreaterThanOrderBySymbol(userId, 0).stream()
                .map(p -> {
                    BigDecimal price = quotes.priceOf(p.getSymbol());
                    return new HoldingResponse(p.getSymbol(), p.getShares(), price,
                            price.multiply(BigDecimal.valueOf(p.getShares())));
                })
                .toList();
        BigDecimal holdingsValue = holdings.stream()
                .map(HoldingResponse::marketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cash = user.getCashBalance();
        return new PortfolioResponse(user.getId(), cash, cash, holdings, cash.add(holdingsValue));
    }
}
