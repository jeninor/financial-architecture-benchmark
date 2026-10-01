package com.tcc.finance.trade.messaging;

import com.tcc.finance.trade.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/** Evento publicado no RabbitMQ (routing key {@code trade.completed}) apos cada compra/venda. */
public record TradeCompletedEvent(Long transactionId, String username, TransactionType type, String symbol,
                                  int quantity, BigDecimal price, BigDecimal total, BigDecimal saldo,
                                  Instant timestamp) {
}
