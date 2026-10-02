package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.quote.QuoteService;
import com.juanesteban.tcc.finance.user.AppUser;
import com.juanesteban.tcc.finance.user.UserService;

@Service
public class PortfolioService {

    private final UserService userService;
    private final HoldingRepository holdingRepository;
    private final QuoteService quoteService;

    public PortfolioService(
        UserService userService,
        HoldingRepository holdingRepository,
        QuoteService quoteService
    ) {
        this.userService = userService;
        this.holdingRepository = holdingRepository;
        this.quoteService = quoteService;
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID userId) {
        AppUser user = userService.getUser(userId);

        List<HoldingResponse> holdings = holdingRepository.findByUserIdOrderBySymbolAsc(userId).stream()
            .filter(h -> h.getShares() > 0)
            .map(h -> {
                BigDecimal price = quoteService.getQuote(h.getSymbol()).price();
                BigDecimal marketValue = price.multiply(BigDecimal.valueOf(h.getShares()));
                return new HoldingResponse(h.getSymbol(), h.getShares(), price, marketValue);
            })
            .toList();

        BigDecimal holdingsValue = holdings.stream()
            .map(HoldingResponse::marketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2);
        BigDecimal cash = user.getCashBalance();

        return new PortfolioResponse(
            user.getId(),
            user.getUsername(),
            cash,
            cash,
            holdings,
            holdingsValue,
            cash.add(holdingsValue)
        );
    }

    public record HoldingResponse(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {
    }

    public record PortfolioResponse(
        UUID userId,
        String username,
        BigDecimal cash,
        BigDecimal cashBalance,
        List<HoldingResponse> holdings,
        BigDecimal holdingsValue,
        BigDecimal totalValue
    ) {
    }
}
