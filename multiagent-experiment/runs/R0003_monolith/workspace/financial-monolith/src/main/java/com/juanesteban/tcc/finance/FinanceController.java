package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class FinanceController {

    public record UserRequest(String username) {
    }

    public record TradeRequest(UUID userId, String symbol, Integer shares) {
    }

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@RequestBody UserRequest req) {
        AppUser u = service.createUser(req.username());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.id);
        m.put("userId", u.id);
        m.put("username", u.username);
        m.put("cash", u.cash);
        m.put("cashBalance", u.cash);
        return m;
    }

    @GetMapping("/quotes/{symbol}")
    public Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal p = service.price(symbol);
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", p);
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> buy(@RequestBody TradeRequest r) {
        return view(service.trade("BUY", r.userId(), r.symbol(), r.shares()));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> sell(@RequestBody TradeRequest r) {
        return view(service.trade("SELL", r.userId(), r.symbol(), r.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<Map<String, Object>> trades(@PathVariable UUID userId) {
        return service.history(userId).stream().map(FinanceController::view).toList();
    }

    private static Map<String, Object> view(Trade t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("userId", t.userId);
        m.put("type", t.type);
        m.put("symbol", t.symbol);
        m.put("shares", t.shares);
        m.put("price", t.price);
        m.put("total", t.total);
        m.put("createdAt", t.createdAt);
        return m;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", String.valueOf(e.getReason())));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> badInput(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
    }
}
