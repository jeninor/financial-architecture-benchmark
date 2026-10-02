package com.juanesteban.tcc.finance.trade;

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

@RestController
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/api/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.buy(request));
    }

    @PostMapping("/api/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.sell(request));
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeResponse> history(@PathVariable UUID userId) {
        return tradeService.history(userId).stream()
            .map(TradeResponse::from)
            .toList();
    }
}
