package com.juanesteban.tcc.finance.dto;

import java.math.BigDecimal;

public record QuoteResponse(String symbol, BigDecimal price) {}
