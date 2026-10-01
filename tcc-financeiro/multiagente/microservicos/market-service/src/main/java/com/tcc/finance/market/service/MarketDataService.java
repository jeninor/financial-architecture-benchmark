package com.tcc.finance.market.service;

import com.tcc.finance.market.exception.SymbolNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Tabela de cotacoes fixa em memoria (sem JPA), para garantir
 * reprodutibilidade experimental entre as execucoes do TCC - ver
 * AGENTE1_ESPECIFICACAO.md. Mantida identica a usada no monolito.
 */
@Service
public class MarketDataService {

    private static final Map<String, BigDecimal> COTACOES = Map.of(
            "AAPL", new BigDecimal("150.00"),
            "GOOG", new BigDecimal("2800.00"),
            "MSFT", new BigDecimal("300.00"),
            "AMZN", new BigDecimal("3300.00")
    );

    public BigDecimal getPrice(String symbol) {
        BigDecimal price = COTACOES.get(normalize(symbol));
        if (price == null) {
            throw new SymbolNotFoundException(symbol);
        }
        return price;
    }

    private String normalize(String symbol) {
        return symbol == null ? null : symbol.toUpperCase();
    }
}
