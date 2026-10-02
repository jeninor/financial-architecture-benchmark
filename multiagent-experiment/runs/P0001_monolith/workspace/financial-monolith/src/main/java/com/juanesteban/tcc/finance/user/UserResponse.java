package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;
import java.time.Instant;

public record UserResponse(Long id, String username, BigDecimal cashBalance, Instant createdAt) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getCashBalance(), user.getCreatedAt());
    }
}
