package com.juanesteban.tcc.finance.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TradeRequest(
    @NotNull Long userId,
    @NotBlank String symbol,
    @NotNull @Positive Long shares
) {
}
