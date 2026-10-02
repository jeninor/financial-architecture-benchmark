package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
    Long id,
    Long userId,
    String symbol,
    TradeSide side,
    TradeSide type,
    TradeSide tradeType,
    long shares,
    BigDecimal price,
    BigDecimal totalAmount,
    Instant executedAt
) {

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
            trade.getId(),
            trade.getUserId(),
            trade.getSymbol(),
            trade.getSide(),
            trade.getSide(),
            trade.getSide(),
            trade.getShares(),
            trade.getPrice(),
            trade.getTotalAmount(),
            trade.getExecutedAt()
        );
    }
}
