package com.tcc.finance.trade.dto;

import java.math.BigDecimal;

/** Corpo enviado ao user-service para debitar/creditar saldo via OpenFeign. */
public class AmountRequest {
    private BigDecimal amount;

    public AmountRequest() {
    }

    public AmountRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
