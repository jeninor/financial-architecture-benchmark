package com.juanesteban.tcc.market;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class QuoteController {

    private static final Map<String, BigDecimal> PRICES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00"));

    public record Quote(String symbol, BigDecimal price) {}

    @GetMapping("/api/quotes/{symbol}")
    public Quote quote(@PathVariable("symbol") String symbol) {
        String s = symbol == null ? "" : symbol.trim().toUpperCase();
        BigDecimal p = PRICES.get(s);
        if (p == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symbol");
        }
        return new Quote(s, p);
    }
}
