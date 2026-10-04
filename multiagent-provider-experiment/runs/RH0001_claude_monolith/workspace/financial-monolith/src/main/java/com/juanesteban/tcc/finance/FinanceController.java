package com.juanesteban.tcc.finance;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api")
public class FinanceController {

    public record UserRequest(String username) {}

    public record TradeRequest(String userId, String symbol, Long shares) {}

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@RequestBody UserRequest req) {
        Domain.AppUser u = service.createUser(req.username());
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
        return FinanceService.view(service.trade(uuid(r.userId()), r.symbol(), r.shares(), true));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> sell(@RequestBody TradeRequest r) {
        return FinanceService.view(service.trade(uuid(r.userId()), r.symbol(), r.shares(), false));
    }

    @GetMapping("/users/{userId}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<Map<String, Object>> trades(@PathVariable UUID userId) {
        return service.history(userId);
    }

    private static UUID uuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (RuntimeException e) {
            throw FinanceService.bad("invalid userId");
        }
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
    }
}
