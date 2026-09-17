package com.juanesteban.tcc.trade.trade;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record SellRequest(

    @NotNull
    UUID userId,

    @NotBlank
    String symbol,

    @NotNull
    @Min(1)
    Integer shares

) {
}