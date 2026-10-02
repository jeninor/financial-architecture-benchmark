package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeResponse(
    UUID id,
    UUID userId,
    String symbol,
    TradeType type,
    long shares,
    BigDecimal price,
    BigDecimal total,
    Instant executedAt
) {

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
            trade.getId(),
            trade.getUserId(),
            trade.getSymbol(),
            trade.getType(),
            trade.getShares(),
            trade.getPrice(),
            trade.getTotal(),
            trade.getExecutedAt()
        );
    }
}
