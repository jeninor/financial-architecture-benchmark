package com.tcc.finance.trade.web;

import com.tcc.finance.trade.service.TradeService;
import com.tcc.finance.trade.web.dto.HistoryEntry;
import com.tcc.finance.trade.web.dto.PortfolioResponse;
import com.tcc.finance.trade.web.dto.TradeRequest;
import com.tcc.finance.trade.web.dto.TradeResponse;
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
    public TradeResponse buy(@RequestBody TradeRequest request) {
        return tradeService.buy(request.username(), request.symbol(), request.quantity());
    }

    @PostMapping("/sell")
    public TradeResponse sell(@RequestBody TradeRequest request) {
        return tradeService.sell(request.username(), request.symbol(), request.quantity());
    }

    @GetMapping("/portfolio/{username}")
    public PortfolioResponse portfolio(@PathVariable String username) {
        return tradeService.portfolio(username);
    }

    @GetMapping("/history/{username}")
    public List<HistoryEntry> history(@PathVariable String username) {
        return tradeService.history(username);
    }
}
