package com.juanesteban.tcc.finance;

import com.juanesteban.tcc.finance.Entities.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {

    public record CreateUserRequest(String username) {}

    public record TradeRequest(UUID userId, String symbol, Long shares) {}

    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createUser(@RequestBody CreateUserRequest req) {
        AppUser u = service.createUser(req.username());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.id);
        m.put("userId", u.id);
        m.put("username", u.username);
        m.put("cash", u.cashBalance);
        m.put("cashBalance", u.cashBalance);
        return m;
    }

    @GetMapping("/quotes/{symbol}")
    public Map<String, Object> quote(@PathVariable String symbol) {
        BigDecimal price = service.quote(symbol);
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", price);
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> buy(@RequestBody TradeRequest req) {
        return toMap(service.trade(req.userId(), req.symbol(), req.shares(), true));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> sell(@RequestBody TradeRequest req) {
        return toMap(service.trade(req.userId(), req.symbol(), req.shares(), false));
    }

    @GetMapping("/users/{userId}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<Map<String, Object>> trades(@PathVariable UUID userId) {
        return service.history(userId);
    }

    private static Map<String, Object> toMap(Trade t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("userId", t.userId);
        m.put("symbol", t.symbol);
        m.put("type", t.type);
        m.put("shares", t.shares);
        m.put("price", t.price);
        return m;
    }
}
