package com.juanesteban.tcc.finance.controller;

import com.juanesteban.tcc.finance.dto.TradeRequest;
import com.juanesteban.tcc.finance.service.TradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    public ResponseEntity<Void> buy(@RequestBody TradeRequest request) {
        tradeService.buy(request.userId(), request.symbol(), request.shares());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sell(@RequestBody TradeRequest request) {
        tradeService.sell(request.userId(), request.symbol(), request.shares());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
