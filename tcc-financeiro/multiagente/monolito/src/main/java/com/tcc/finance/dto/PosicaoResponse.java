package com.tcc.finance.dto;

import java.math.BigDecimal;

public class PosicaoResponse {
    private String symbol;
    private long quantity;
    private BigDecimal precoAtual;
    private BigDecimal valorTotal;

    public PosicaoResponse(String symbol, long quantity, BigDecimal precoAtual, BigDecimal valorTotal) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.precoAtual = precoAtual;
        this.valorTotal = valorTotal;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPrecoAtual() {
        return precoAtual;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
