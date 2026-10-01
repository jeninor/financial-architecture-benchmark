package com.tcc.finance.web.dto;

import com.tcc.finance.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record HistoryEntry(Long id, TransactionType type, String symbol, int quantity, BigDecimal price,
                           Instant timestamp) {
}
