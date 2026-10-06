package com.juanesteban.tcc.market;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@RestController
public class QuoteController {

    private static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00"));

    @GetMapping("/api/quotes/{symbol}")
    public Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal price = QUOTES.get(symbol.toUpperCase());
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown symbol");
        }
        return Map.of("symbol", symbol.toUpperCase(), "price", price);
    }
}
