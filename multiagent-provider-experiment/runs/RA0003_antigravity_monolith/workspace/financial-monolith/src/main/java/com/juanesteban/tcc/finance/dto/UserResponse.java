package com.juanesteban.tcc.finance.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(UUID id, String username, BigDecimal cash) {}
