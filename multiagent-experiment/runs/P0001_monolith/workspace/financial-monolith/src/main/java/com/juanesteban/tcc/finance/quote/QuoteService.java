package com.juanesteban.tcc.finance.quote;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.juanesteban.tcc.finance.common.ApiException;

/**
 * Fixed quotes defined by the functional contract. No external market data.
 */
@Service
public class QuoteService {

    private static final Map<String, BigDecimal> PRICES = Map.of(
        "AAPL", new BigDecimal("200.00"),
        "MSFT", new BigDecimal("400.00"),
        "GOOGL", new BigDecimal("170.00"),
        "AMZN", new BigDecimal("190.00"),
        "NVDA", new BigDecimal("120.00")
    );

    public Optional<QuoteResponse> find(String symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        String normalized = normalize(symbol);
        BigDecimal price = PRICES.get(normalized);
        return price == null ? Optional.empty() : Optional.of(new QuoteResponse(normalized, price));
    }

    public QuoteResponse get(String symbol) {
        return find(symbol).orElseThrow(() -> ApiException.badRequest("Unknown symbol: " + symbol));
    }

    public static String normalize(String symbol) {
        return symbol.trim().toUpperCase(Locale.ROOT);
    }
}
