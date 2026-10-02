package com.tcc.finance.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TransacaoResponse {
    private String symbol;
    private String tipo;
    private long quantity;
    private BigDecimal preco;
    private Instant timestamp;

    public TransacaoResponse(String symbol, String tipo, long quantity, BigDecimal preco, Instant timestamp) {
        this.symbol = symbol;
        this.tipo = tipo;
        this.quantity = quantity;
        this.preco = preco;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTipo() {
        return tipo;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
