package com.tcc.finance.web.dto;

import java.math.BigDecimal;

public record QuoteResponse(String symbol, BigDecimal price) {
}
