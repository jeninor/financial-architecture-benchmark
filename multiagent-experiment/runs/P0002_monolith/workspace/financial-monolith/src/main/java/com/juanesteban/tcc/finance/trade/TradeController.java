package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/api/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.buy(request.userId(), request.symbol(), request.shares()));
    }

    @PostMapping("/api/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.sell(request.userId(), request.symbol(), request.shares()));
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeResponse> history(@PathVariable UUID userId) {
        return tradeService.history(userId).stream().map(TradeResponse::from).toList();
    }

    public record TradeRequest(@NotNull UUID userId, @NotBlank String symbol, @NotNull Integer shares) {
    }

    public record TradeResponse(
        UUID id,
        UUID userId,
        TradeType type,
        String symbol,
        long shares,
        BigDecimal price,
        BigDecimal total,
        Instant createdAt
    ) {
        static TradeResponse from(Trade trade) {
            return new TradeResponse(
                trade.getId(),
                trade.getUserId(),
                trade.getType(),
                trade.getSymbol(),
                trade.getShares(),
                trade.getPrice(),
                trade.getTotal(),
                trade.getCreatedAt()
            );
        }
    }
}
