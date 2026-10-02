package com.juanesteban.tcc.finance.quote;

import java.math.BigDecimal;

public record QuoteResponse(String symbol, BigDecimal price) {
}
