package com.juanesteban.tcc.user.user;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceChangeRequest(

    @NotNull
    @DecimalMin(
        value = "0.01",
        inclusive = true
    )
    BigDecimal amount

) {
}