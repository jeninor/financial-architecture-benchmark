package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {

    public record UserRequest(String username) {}

    public record TradeRequest(UUID userId, String symbol, Integer shares) {}

    private final FinanceService service;

    FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest req) {
        User u = service.createUser(req.username());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.getId());
        body.put("userId", u.getId());
        body.put("username", u.getUsername());
        body.put("cash", u.getCashBalance());
        body.put("cashBalance", u.getCashBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/quotes/{symbol}")
    public Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal price = service.quote(symbol);
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", price);
    }

    @PostMapping("/trades/buy")
    public ResponseEntity<Trade> buy(@RequestBody TradeRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.trade("BUY", r.userId(), r.symbol(), r.shares()));
    }

    @PostMapping("/trades/sell")
    public ResponseEntity<Trade> sell(@RequestBody TradeRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.trade("SELL", r.userId(), r.symbol(), r.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<Trade> trades(@PathVariable UUID userId) {
        return service.history(userId);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handle(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
    }
}
