package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.quote.Quote;
import com.juanesteban.tcc.finance.quote.QuoteService;
import com.juanesteban.tcc.finance.user.UserAccount;
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
        UserAccount user = userService.getUser(userId);

        List<PortfolioResponse.HoldingView> holdings = holdingRepository
            .findByUserIdOrderBySymbolAsc(userId)
            .stream()
            .filter(h -> h.getShares() > 0)
            .map(h -> {
                Quote quote = quoteService.getQuote(h.getSymbol());
                BigDecimal marketValue = quote.price().multiply(BigDecimal.valueOf(h.getShares()));
                return new PortfolioResponse.HoldingView(
                    h.getSymbol(), h.getShares(), quote.price(), marketValue);
            })
            .toList();

        BigDecimal holdingsValue = holdings.stream()
            .map(PortfolioResponse.HoldingView::marketValue)
            .reduce(new BigDecimal("0.00"), BigDecimal::add);

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
}
