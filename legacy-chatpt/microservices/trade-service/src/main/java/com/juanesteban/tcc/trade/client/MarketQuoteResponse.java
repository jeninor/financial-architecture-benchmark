package com.juanesteban.tcc.trade.client;

import java.math.BigDecimal;

public record MarketQuoteResponse(

    String symbol,

    String name,

    BigDecimal price

) {
}