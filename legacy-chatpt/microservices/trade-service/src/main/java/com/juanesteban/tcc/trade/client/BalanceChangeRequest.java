package com.juanesteban.tcc.trade.client;

import java.math.BigDecimal;

public record BalanceChangeRequest(

    BigDecimal amount

) {
}