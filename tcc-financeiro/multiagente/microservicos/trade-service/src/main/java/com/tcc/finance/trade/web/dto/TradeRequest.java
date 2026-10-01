package com.tcc.finance.trade.web.dto;

public record TradeRequest(String username, String symbol, Integer quantity) {
}
