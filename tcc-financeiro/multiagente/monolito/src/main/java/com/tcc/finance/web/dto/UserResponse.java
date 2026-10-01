package com.tcc.finance.web.dto;

import java.math.BigDecimal;

public record UserResponse(Long id, String username, BigDecimal saldo) {
}
