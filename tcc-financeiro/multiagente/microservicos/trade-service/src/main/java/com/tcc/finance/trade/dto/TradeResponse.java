package com.tcc.finance.trade.dto;

import java.math.BigDecimal;

public class TradeResponse {
    private String username;
    private String symbol;
    private long quantity;
    private BigDecimal preco;
    private BigDecimal saldo;

    public TradeResponse(String username, String symbol, long quantity, BigDecimal preco, BigDecimal saldo) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.preco = preco;
        this.saldo = saldo;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
