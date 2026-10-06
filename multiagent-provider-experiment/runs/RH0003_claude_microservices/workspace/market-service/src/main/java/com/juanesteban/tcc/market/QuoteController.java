package com.juanesteban.tcc.market;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {

    private static final Map<String, BigDecimal> QUOTES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    @GetMapping("/api/quotes/{symbol}")
    public ResponseEntity<?> quote(@PathVariable String symbol) {
        String key = symbol.trim().toUpperCase();
        BigDecimal price = QUOTES.get(key);
        if (price == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown symbol: " + symbol));
        }
        return ResponseEntity.ok(Map.of("symbol", key, "price", price));
    }
}
