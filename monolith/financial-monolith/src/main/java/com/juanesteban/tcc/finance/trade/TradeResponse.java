package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResponse(

    UUID id,

    UUID userId,

    String symbol,

    TradeType type,

    Integer shares,

    BigDecimal price,

    BigDecimal total,

    BigDecimal cashAfter,

    LocalDateTime createdAt

) {
}