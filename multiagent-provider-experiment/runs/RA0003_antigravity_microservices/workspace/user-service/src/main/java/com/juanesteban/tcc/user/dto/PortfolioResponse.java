package com.juanesteban.tcc.user.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private BigDecimal cashBalance;
    private List<HoldingDto> holdings;
    private BigDecimal totalValue;

    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
    public List<HoldingDto> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingDto> holdings) { this.holdings = holdings; }
    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
}
