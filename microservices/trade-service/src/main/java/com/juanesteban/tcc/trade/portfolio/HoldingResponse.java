package com.juanesteban.tcc.trade.portfolio;

import java.math.BigDecimal;

public record HoldingResponse(

    String symbol,

    Integer shares,

    BigDecimal price,

    BigDecimal value

) {
}