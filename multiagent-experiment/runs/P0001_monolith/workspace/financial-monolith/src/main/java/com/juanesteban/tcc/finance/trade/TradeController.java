package com.juanesteban.tcc.finance.trade;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.buy(request));
    }

    @PostMapping("/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@Valid @RequestBody TradeRequest request) {
        return TradeResponse.from(tradeService.sell(request));
    }
}
