package com.juanesteban.tcc.user.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

    UUID id,

    String username,

    BigDecimal cash,

    LocalDateTime createdAt

) {

    public static UserResponse from(
        User user
    ) {

        return new UserResponse(

            user.getId(),

            user.getUsername(),

            user.getCash(),

            user.getCreatedAt()
        );
    }
}