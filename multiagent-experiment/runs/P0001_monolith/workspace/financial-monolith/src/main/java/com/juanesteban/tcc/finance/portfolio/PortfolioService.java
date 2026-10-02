package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.quote.QuoteService;
import com.juanesteban.tcc.finance.user.UserAccount;
import com.juanesteban.tcc.finance.user.UserService;

@Service
public class PortfolioService {

    private final UserService userService;
    private final HoldingRepository holdingRepository;
    private final QuoteService quoteService;

    public PortfolioService(UserService userService, HoldingRepository holdingRepository, QuoteService quoteService) {
        this.userService = userService;
        this.holdingRepository = holdingRepository;
        this.quoteService = quoteService;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long userId) {
        UserAccount user = userService.get(userId);

        List<PortfolioResponse.HoldingResponse> holdings = holdingRepository.findByUserIdOrderBySymbolAsc(userId).stream()
            .filter(h -> h.getShares() > 0)
            .map(h -> {
                BigDecimal price = quoteService.get(h.getSymbol()).price();
                BigDecimal marketValue = price.multiply(BigDecimal.valueOf(h.getShares())).setScale(2, RoundingMode.HALF_UP);
                return new PortfolioResponse.HoldingResponse(h.getSymbol(), h.getShares(), price, marketValue);
            })
            .toList();

        BigDecimal holdingsValue = holdings.stream()
            .map(PortfolioResponse.HoldingResponse::marketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal cash = user.getCashBalance().setScale(2, RoundingMode.HALF_UP);

        return new PortfolioResponse(user.getId(), user.getUsername(), cash, holdings, holdingsValue, cash.add(holdingsValue));
    }
}
