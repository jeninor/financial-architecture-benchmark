package com.tcc.finance.trade.client.dto;

import java.math.BigDecimal;

public record UserDto(Long id, String username, BigDecimal saldo) {
}
