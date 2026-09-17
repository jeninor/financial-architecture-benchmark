package com.juanesteban.tcc.finance.trade;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;


    public TradeController(
        TradeService tradeService
    ) {

        this.tradeService =
            tradeService;
    }


    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(
        @Valid
        @RequestBody
        BuyRequest request
    ) {

        return tradeService.buy(request);
    }
}