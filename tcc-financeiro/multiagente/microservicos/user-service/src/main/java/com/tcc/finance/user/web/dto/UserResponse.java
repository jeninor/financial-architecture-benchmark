package com.tcc.finance.user.web.dto;

import java.math.BigDecimal;

public record UserResponse(Long id, String username, BigDecimal saldo) {
}
