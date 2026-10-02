package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    UUID userId,
    String username,
    BigDecimal cashBalance,
    Instant createdAt
) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(
            user.getId(),
            user.getId(),
            user.getUsername(),
            user.getCashBalance(),
            user.getCreatedAt()
        );
    }
}
