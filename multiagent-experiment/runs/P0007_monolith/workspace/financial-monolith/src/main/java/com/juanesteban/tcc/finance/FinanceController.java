package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class FinanceController {
    record UserRequest(String username) {}

    record TradeRequest(UUID userId, String symbol, Integer shares) {}

    private final FinanceService service;

    FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest req) {
        AppUser u = service.createUser(req.username());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.id);
        body.put("userId", u.id);
        body.put("username", u.username);
        body.put("cash", u.cash);
        body.put("cashBalance", u.cash);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/quotes/{symbol}")
    Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal p = service.price(symbol);
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", p);
    }

    @PostMapping("/trades/buy")
    ResponseEntity<Map<String, Object>> buy(@RequestBody TradeRequest r) {
        return trade(true, r);
    }

    @PostMapping("/trades/sell")
    ResponseEntity<Map<String, Object>> sell(@RequestBody TradeRequest r) {
        return trade(false, r);
    }

    private ResponseEntity<Map<String, Object>> trade(boolean buy, TradeRequest r) {
        TradeRecord t = service.trade(buy, r.userId(), r.symbol(), r.shares());
        return ResponseEntity.status(HttpStatus.CREATED).body(FinanceService.view(t));
    }

    @GetMapping("/users/{userId}/portfolio")
    Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    List<Map<String, Object>> trades(@PathVariable UUID userId) {
        return service.history(userId);
    }
}
