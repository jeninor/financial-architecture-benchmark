package com.tcc.finance.web.dto;

import com.tcc.finance.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(Long transactionId, String username, TransactionType type, String symbol,
                            int quantity, BigDecimal price, BigDecimal total, BigDecimal saldo,
                            Instant timestamp) {
}
