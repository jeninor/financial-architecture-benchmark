package com.juanesteban.tcc.finance.domain;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private static final Map<String, BigDecimal> QUOTES = Map.of(
            "AAPL", new BigDecimal("200.00"),
            "MSFT", new BigDecimal("400.00"),
            "GOOGL", new BigDecimal("170.00"),
            "AMZN", new BigDecimal("190.00"),
            "NVDA", new BigDecimal("120.00"));

    public BigDecimal priceOf(String symbol) {
        BigDecimal price = symbol == null ? null : QUOTES.get(symbol);
        if (price == null) {
            throw new BadRequestException("Unknown symbol: " + symbol);
        }
        return price;
    }
}
