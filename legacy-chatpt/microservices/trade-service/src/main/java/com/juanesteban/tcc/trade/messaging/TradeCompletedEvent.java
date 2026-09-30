package com.juanesteban.tcc.trade.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeCompletedEvent(

    UUID tradeId,

    UUID userId,

    String symbol,

    String type,

    Integer shares,

    BigDecimal price,

    BigDecimal total,

    LocalDateTime occurredAt

) {
}