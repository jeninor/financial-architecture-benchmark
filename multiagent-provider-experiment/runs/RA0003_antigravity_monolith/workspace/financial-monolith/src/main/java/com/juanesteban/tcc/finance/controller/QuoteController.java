package com.juanesteban.tcc.finance.controller;

import com.juanesteban.tcc.finance.dto.QuoteResponse;
import com.juanesteban.tcc.finance.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String symbol) {
        BigDecimal price = quoteService.getQuote(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Invalid symbol"));
        return ResponseEntity.ok(new QuoteResponse(symbol, price));
    }
}
