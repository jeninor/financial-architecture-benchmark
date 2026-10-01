package com.tcc.finance.controller;

import com.tcc.finance.dto.PortfolioResponse;
import com.tcc.finance.dto.TradeRequest;
import com.tcc.finance.dto.TradeResponse;
import com.tcc.finance.dto.TransacaoResponse;
import com.tcc.finance.service.TradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(@RequestBody TradeRequest request) {
        TradeResponse response = tradeService.buy(request.getUsername(), request.getSymbol(), request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(@RequestBody TradeRequest request) {
        TradeResponse response = tradeService.sell(request.getUsername(), request.getSymbol(), request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/portfolio/{username}")
    public ResponseEntity<PortfolioResponse> portfolio(@PathVariable String username) {
        return ResponseEntity.ok(tradeService.portfolio(username));
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<List<TransacaoResponse>> history(@PathVariable String username) {
        return ResponseEntity.ok(tradeService.historico(username));
    }
}
