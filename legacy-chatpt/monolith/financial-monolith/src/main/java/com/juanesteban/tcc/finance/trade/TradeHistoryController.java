package com.juanesteban.tcc.finance.trade;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class TradeHistoryController {

    private final TradeService tradeService;


    public TradeHistoryController(
        TradeService tradeService
    ) {

        this.tradeService =
            tradeService;
    }


    @GetMapping("/{userId}/trades")
    public List<TradeHistoryResponse> getHistory(
        @PathVariable UUID userId
    ) {

        return tradeService
            .getHistory(userId);
    }
}