package com.tcc.finance.web;

import com.tcc.finance.service.QuoteService;
import com.tcc.finance.web.dto.QuoteResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/quote/{symbol}")
    public QuoteResponse quote(@PathVariable String symbol) {
        return new QuoteResponse(quoteService.normalize(symbol), quoteService.getPrice(symbol));
    }
}
