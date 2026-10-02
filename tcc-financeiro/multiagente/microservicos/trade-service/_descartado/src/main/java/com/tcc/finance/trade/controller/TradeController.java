package com.tcc.finance.trade.controller;

import com.tcc.finance.trade.dto.PortfolioResponse;
import com.tcc.finance.trade.dto.TradeRequest;
import com.tcc.finance.trade.dto.TradeResponse;
import com.tcc.finance.trade.dto.TransacaoResponse;
import com.tcc.finance.trade.service.TradeService;
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
        return ResponseEntity.ok(tradeService.buy(request.getUsername(), request.getSymbol(), request.getQuantity()));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(@RequestBody TradeRequest request) {
        return ResponseEntity.ok(tradeService.sell(request.getUsername(), request.getSymbol(), request.getQuantity()));
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
