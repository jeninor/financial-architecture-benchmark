package com.juanesteban.tcc.finance.dto;

import java.util.UUID;

public record TradeRequest(UUID userId, String symbol, int shares) {}
