package com.juanesteban.tcc.trade.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

    UUID id,

    String username,

    BigDecimal cash,

    LocalDateTime createdAt

) {
}