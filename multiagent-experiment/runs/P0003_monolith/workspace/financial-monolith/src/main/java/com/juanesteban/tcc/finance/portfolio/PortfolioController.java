package com.juanesteban.tcc.finance.portfolio;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/api/users/{userId}/portfolio")
    public PortfolioResponse getPortfolio(@PathVariable UUID userId) {
        return portfolioService.getPortfolio(userId);
    }
}
