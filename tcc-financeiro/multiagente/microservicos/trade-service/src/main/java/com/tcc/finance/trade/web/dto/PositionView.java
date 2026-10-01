package com.tcc.finance.trade.web.dto;

import java.math.BigDecimal;

public record PositionView(String symbol, int quantity, BigDecimal currentPrice, BigDecimal totalValue) {
}
