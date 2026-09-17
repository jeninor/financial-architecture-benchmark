package com.juanesteban.tcc.market.quote;

import java.math.BigDecimal;

public record Quote(

    String symbol,

    String name,

    BigDecimal price

) {
}