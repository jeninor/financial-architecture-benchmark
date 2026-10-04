package com.juanesteban.tcc.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(BigDecimal cash, List<PortfolioHolding> holdings, BigDecimal totalValue) {}
