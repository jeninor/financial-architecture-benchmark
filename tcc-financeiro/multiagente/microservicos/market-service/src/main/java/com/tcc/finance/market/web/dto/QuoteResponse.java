package com.tcc.finance.market.web.dto;

import java.math.BigDecimal;

public record QuoteResponse(String symbol, BigDecimal price) {
}
