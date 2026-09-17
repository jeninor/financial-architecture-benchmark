package com.juanesteban.tcc.finance.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(

    UUID userId,

    BigDecimal cash,

    List<HoldingResponse> holdings,

    BigDecimal holdingsValue,

    BigDecimal totalValue

) {
}