package com.juanesteban.tcc.trade.trade;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService
        tradeService;


    public TradeController(
        TradeService tradeService
    ) {

        this.tradeService =
            tradeService;
    }


    @PostMapping("/buy")
    @ResponseStatus(
        HttpStatus.CREATED
    )
    public TradeResponse buy(

        @Valid
        @RequestBody
        BuyRequest request
    ) {

        return tradeService.buy(
            request
        );
    }


    @PostMapping("/sell")
    @ResponseStatus(
        HttpStatus.CREATED
    )
    public TradeResponse sell(

        @Valid
        @RequestBody
        SellRequest request
    ) {

        return tradeService.sell(
            request
        );
    }
}