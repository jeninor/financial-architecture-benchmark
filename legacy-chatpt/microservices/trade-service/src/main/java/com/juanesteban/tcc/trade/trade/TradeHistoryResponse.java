package com.juanesteban.tcc.trade.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeHistoryResponse(

    UUID id,

    String symbol,

    TradeType type,

    Integer shares,

    BigDecimal price,

    BigDecimal total,

    LocalDateTime createdAt

) {
}