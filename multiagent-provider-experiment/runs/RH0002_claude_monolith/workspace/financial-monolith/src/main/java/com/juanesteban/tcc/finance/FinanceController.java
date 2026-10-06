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
class FinanceController {

    record UserRequest(String username) {}
    record TradeRequest(UUID userId, String symbol, Long shares) {}

    private final FinanceService service;

    FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest req) {
        AppUser u = service.createUser(req.username());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.id);
        m.put("userId", u.id);
        m.put("username", u.username);
        m.put("cash", u.cash);
        m.put("cashBalance", u.cash);
        return ResponseEntity.status(HttpStatus.CREATED).body(m);
    }

    @GetMapping("/quotes/{symbol}")
    Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal p = FinanceService.price(symbol);
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", p);
    }

    @PostMapping("/trades/buy")
    ResponseEntity<Map<String, Object>> buy(@RequestBody TradeRequest r) {
        return exec(r, true);
    }

    @PostMapping("/trades/sell")
    ResponseEntity<Map<String, Object>> sell(@RequestBody TradeRequest r) {
        return exec(r, false);
    }

    private ResponseEntity<Map<String, Object>> exec(TradeRequest r, boolean buy) {
        if (r.shares() == null) throw FinanceService.bad("shares required");
        Trade t = service.trade(r.userId(), r.symbol(), r.shares(), buy);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("userId", t.userId);
        m.put("type", t.type);
        m.put("symbol", t.symbol);
        m.put("shares", t.shares);
        m.put("price", t.price);
        m.put("total", t.total);
        return ResponseEntity.status(HttpStatus.CREATED).body(m);
    }

    @GetMapping("/users/{userId}/portfolio")
    Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    List<Map<String, Object>> trades(@PathVariable UUID userId) {
        return service.history(userId);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, String>> badInput(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", String.valueOf(e.getReason())));
    }
}
