package com.juanesteban.tcc.finance.quote;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.juanesteban.tcc.finance.common.BusinessException;

@Service
public class QuoteService {

    private static final Map<String, BigDecimal> FIXED_PRICES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    public Quote getQuote(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new BusinessException("Symbol is required");
        }
        String normalized = symbol.trim().toUpperCase(Locale.ROOT);
        BigDecimal price = FIXED_PRICES.get(normalized);
        if (price == null) {
            throw new BusinessException("Unknown symbol: " + symbol);
        }
        return new Quote(normalized, price);
    }
}
