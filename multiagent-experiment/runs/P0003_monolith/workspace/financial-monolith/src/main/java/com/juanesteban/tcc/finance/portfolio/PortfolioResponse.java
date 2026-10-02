package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(
    UUID userId,
    String username,
    BigDecimal cash,
    BigDecimal cashBalance,
    List<HoldingView> holdings,
    BigDecimal holdingsValue,
    BigDecimal totalValue
) {

    public record HoldingView(
        String symbol,
        long shares,
        BigDecimal price,
        BigDecimal marketValue
    ) {
    }
}
