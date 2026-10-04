package com.juanesteban.tcc.finance.dto;

import java.math.BigDecimal;

public record PortfolioHolding(String symbol, int shares, BigDecimal price, BigDecimal marketValue) {}
