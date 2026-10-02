package com.tcc.finance.trade.dto;

import java.math.BigDecimal;

/** Espelha a resposta do market-service (GET /quote/{symbol}). */
public class QuoteDTO {
    private String symbol;
    private BigDecimal price;

    public QuoteDTO() {
    }

    public QuoteDTO(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
