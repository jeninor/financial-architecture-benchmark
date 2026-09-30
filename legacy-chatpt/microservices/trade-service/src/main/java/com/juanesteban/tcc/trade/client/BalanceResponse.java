package com.juanesteban.tcc.trade.client;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(

    UUID userId,

    BigDecimal cash

) {
}