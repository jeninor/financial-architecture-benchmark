package com.juanesteban.tcc.finance.market;

import java.math.BigDecimal;

public record Quote(
    String symbol,
    String name,
    BigDecimal price
) {
}