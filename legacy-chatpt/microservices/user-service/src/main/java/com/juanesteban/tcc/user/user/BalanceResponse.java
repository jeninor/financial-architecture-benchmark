package com.juanesteban.tcc.user.user;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(

    UUID userId,

    BigDecimal cash

) {

    public static BalanceResponse from(
        User user
    ) {

        return new BalanceResponse(

            user.getId(),

            user.getCash()
        );
    }
}