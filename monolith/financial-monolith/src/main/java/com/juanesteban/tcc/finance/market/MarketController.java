package com.juanesteban.tcc.finance.market;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class MarketController {

    private final QuoteProvider quoteProvider;


    public MarketController(
        QuoteProvider quoteProvider
    ) {

        this.quoteProvider =
            quoteProvider;
    }


    @GetMapping("/{symbol}")
    public Quote getQuote(
        @PathVariable String symbol
    ) {

        return quoteProvider.getQuote(symbol);
    }
}