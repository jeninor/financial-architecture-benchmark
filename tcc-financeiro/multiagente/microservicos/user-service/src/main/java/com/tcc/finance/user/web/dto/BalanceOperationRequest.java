package com.tcc.finance.user.web.dto;

import java.math.BigDecimal;

public record BalanceOperationRequest(BigDecimal amount) {
}
