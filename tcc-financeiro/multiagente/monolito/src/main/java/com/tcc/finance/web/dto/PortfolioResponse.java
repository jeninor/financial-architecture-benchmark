package com.tcc.finance.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(String username, List<PositionView> positions, BigDecimal saldo,
                                BigDecimal stocksValue, BigDecimal totalValue) {

    public PortfolioResponse {
        positions = positions == null ? List.of() : List.copyOf(positions);
    }

    @Override
    public List<PositionView> positions() {
        return List.copyOf(positions);
    }
}
