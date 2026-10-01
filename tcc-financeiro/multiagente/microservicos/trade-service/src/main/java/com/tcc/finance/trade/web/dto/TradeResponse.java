package com.tcc.finance.trade.web.dto;

import com.tcc.finance.trade.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(Long transactionId, String username, TransactionType type, String symbol,
                            int quantity, BigDecimal price, BigDecimal total, BigDecimal saldo,
                            Instant timestamp) {
}
