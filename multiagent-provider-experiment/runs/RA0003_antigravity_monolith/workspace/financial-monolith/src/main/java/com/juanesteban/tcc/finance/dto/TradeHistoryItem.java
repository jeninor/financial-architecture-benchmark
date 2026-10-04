package com.juanesteban.tcc.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeHistoryItem(String type, String symbol, int shares, BigDecimal price, LocalDateTime timestamp) {}
