package com.juanesteban.tcc.finance.portfolio;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class PortfolioController {

    private final PortfolioService portfolioService;


    public PortfolioController(
        PortfolioService portfolioService
    ) {

        this.portfolioService =
            portfolioService;
    }


    @GetMapping("/{userId}/portfolio")
    public PortfolioResponse getPortfolio(
        @PathVariable UUID userId
    ) {

        return portfolioService
            .getPortfolio(userId);
    }
}