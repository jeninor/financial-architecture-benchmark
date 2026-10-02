package com.juanesteban.tcc.finance.portfolio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juanesteban.tcc.finance.trade.TradeResponse;
import com.juanesteban.tcc.finance.trade.TradeService;

@RestController
@RequestMapping("/api/users/{userId}")
public class UserQueryController {

    private final PortfolioService portfolioService;
    private final TradeService tradeService;

    public UserQueryController(PortfolioService portfolioService, TradeService tradeService) {
        this.portfolioService = portfolioService;
        this.tradeService = tradeService;
    }

    @GetMapping("/portfolio")
    public PortfolioResponse portfolio(@PathVariable Long userId) {
        return portfolioService.getPortfolio(userId);
    }

    @GetMapping("/trades")
    public List<TradeResponse> trades(@PathVariable Long userId) {
        return tradeService.history(userId).stream().map(TradeResponse::from).toList();
    }
}
