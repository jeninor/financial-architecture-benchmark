package com.tcc.finance.market.service;

import com.tcc.finance.market.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Tabela fixa de cotacoes simuladas, mantida em memoria para garantir
 * reprodutibilidade experimental.
 */
@Service
public class QuoteService {

    private static final Map<String, BigDecimal> PRICES = Map.of(
            "AAPL", new BigDecimal("150.00"),
            "GOOG", new BigDecimal("2800.00"),
            "MSFT", new BigDecimal("300.00"),
            "AMZN", new BigDecimal("3300.00")
    );

    public String normalize(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
    }

    public BigDecimal getPrice(String symbol) {
        BigDecimal price = PRICES.get(normalize(symbol));
        if (price == null) {
            throw new NotFoundException("Simbolo nao encontrado: " + symbol);
        }
        return price;
    }
}
