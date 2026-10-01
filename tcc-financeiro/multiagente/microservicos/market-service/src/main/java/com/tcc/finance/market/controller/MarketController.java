package com.tcc.finance.market.controller;

import com.tcc.finance.market.dto.QuoteResponse;
import com.tcc.finance.market.service.MarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/quote/{symbol}")
    public ResponseEntity<QuoteResponse> quote(@PathVariable String symbol) {
        var price = marketDataService.getPrice(symbol);
        return ResponseEntity.ok(new QuoteResponse(symbol.toUpperCase(), price));
    }
}
