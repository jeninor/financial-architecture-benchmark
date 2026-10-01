package com.tcc.finance.trade.client.dto;

import java.math.BigDecimal;

public record QuoteDto(String symbol, BigDecimal price) {
}
