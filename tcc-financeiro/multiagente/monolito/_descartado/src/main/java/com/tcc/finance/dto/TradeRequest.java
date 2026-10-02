package com.tcc.finance.dto;

public class TradeRequest {
    private String username;
    private String symbol;
    private long quantity;

    public TradeRequest() {
    }

    public TradeRequest(String username, String symbol, long quantity) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
