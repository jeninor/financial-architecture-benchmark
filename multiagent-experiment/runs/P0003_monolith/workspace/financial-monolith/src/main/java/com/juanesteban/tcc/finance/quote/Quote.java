package com.juanesteban.tcc.finance.quote;

import java.math.BigDecimal;

public record Quote(String symbol, BigDecimal price) {
}
