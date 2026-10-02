package com.juanesteban.tcc.finance.trade;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TradeRequest(
    @NotNull UUID userId,
    @NotBlank String symbol,
    @NotNull @Positive Integer shares
) {
}
