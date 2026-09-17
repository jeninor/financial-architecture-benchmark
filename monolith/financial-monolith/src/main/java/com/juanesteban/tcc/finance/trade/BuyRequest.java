package com.juanesteban.tcc.finance.trade;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BuyRequest(

    @NotNull
    UUID userId,

    @NotBlank
    String symbol,

    @NotNull
    @Min(1)
    Integer shares

) {
}