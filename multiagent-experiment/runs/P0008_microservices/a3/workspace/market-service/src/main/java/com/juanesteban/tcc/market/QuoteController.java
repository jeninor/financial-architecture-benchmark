package com.juanesteban.tcc.market;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    public record Quote(String symbol, BigDecimal price) {
    }

    private static final Map<String, BigDecimal> PRICES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    @GetMapping("/{symbol}")
    public Quote quote(@PathVariable("symbol") String symbol) {
        BigDecimal price = PRICES.get(symbol);
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symbol");
        }
        return new Quote(symbol, price);
    }
}
