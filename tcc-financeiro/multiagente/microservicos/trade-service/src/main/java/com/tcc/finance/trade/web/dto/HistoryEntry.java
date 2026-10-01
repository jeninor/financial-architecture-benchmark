package com.tcc.finance.trade.web.dto;

import com.tcc.finance.trade.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record HistoryEntry(Long id, TransactionType type, String symbol, int quantity, BigDecimal price,
                           Instant timestamp) {
}
