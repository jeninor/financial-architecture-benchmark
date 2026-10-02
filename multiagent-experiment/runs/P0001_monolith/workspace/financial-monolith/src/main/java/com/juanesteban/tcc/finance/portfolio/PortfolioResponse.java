package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
    Long userId,
    String username,
    BigDecimal cashBalance,
    List<HoldingResponse> holdings,
    BigDecimal holdingsValue,
    BigDecimal totalValue
) {

    public record HoldingResponse(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {
    }
}
