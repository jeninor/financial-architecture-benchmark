package com.tcc.finance.user.dto;

import java.math.BigDecimal;

/**
 * Usado pelo trade-service, via OpenFeign, para debitar/creditar o saldo
 * do usuario ao concluir uma compra ou venda.
 */
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
