package com.juanesteban.tcc.finance.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Dtos {

    private Dtos() {
    }

    public record CreateUserRequest(String username) {
    }

    public record UserResponse(UUID id, UUID userId, String username, BigDecimal cashBalance, BigDecimal cash) {
    }

    public record QuoteResponse(String symbol, BigDecimal price) {
    }

    public record TradeRequest(UUID userId, String symbol, Long shares) {
    }

    public record TradeResponse(UUID id, UUID userId, String symbol, String type, long shares,
                                BigDecimal price, BigDecimal total, Instant createdAt) {
    }

    public record HoldingResponse(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {
    }

    public record PortfolioResponse(UUID userId, BigDecimal cash, BigDecimal cashBalance,
                                    List<HoldingResponse> holdings, BigDecimal totalValue) {
    }
}
