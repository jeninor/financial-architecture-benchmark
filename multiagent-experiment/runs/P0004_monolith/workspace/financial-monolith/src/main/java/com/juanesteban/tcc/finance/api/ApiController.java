package com.juanesteban.tcc.finance.api;

import com.juanesteban.tcc.finance.domain.*;
import com.juanesteban.tcc.finance.domain.FinanceService.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record UserRequest(String username) {}

    public record TradeRequest(UUID userId, String symbol, Integer shares) {}

    public record TradeView(UUID id, UUID userId, String type, String symbol, int shares,
                            BigDecimal price, BigDecimal total, java.time.Instant createdAt) {}

    private final FinanceService service;

    public ApiController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest req) {
        AppUser u = service.createUser(req.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", u.getId(), "userId", u.getId(), "username", u.getUsername(),
                "cash", u.getCash(), "cashBalance", u.getCash(), "balance", u.getCash()));
    }

    @GetMapping("/quotes/{symbol}")
    public Map<String, Object> quote(@PathVariable String symbol) {
        return Map.of("symbol", symbol.trim().toUpperCase(), "price", service.quote(symbol));
    }

    @PostMapping("/trades/buy")
    public ResponseEntity<TradeView> buy(@RequestBody TradeRequest r) {
        return created(service.trade(Trade.Type.BUY, r.userId(), r.symbol(), r.shares()));
    }

    @PostMapping("/trades/sell")
    public ResponseEntity<TradeView> sell(@RequestBody TradeRequest r) {
        return created(service.trade(Trade.Type.SELL, r.userId(), r.symbol(), r.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public FinanceService.PortfolioView portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<TradeView> trades(@PathVariable UUID userId) {
        return service.history(userId).stream().map(ApiController::view).toList();
    }

    private static ResponseEntity<TradeView> created(Trade t) {
        return ResponseEntity.status(HttpStatus.CREATED).body(view(t));
    }

    private static TradeView view(Trade t) {
        return new TradeView(t.getId(), t.getUserId(), t.getType().name(), t.getSymbol(), t.getShares(),
                t.getPrice(), t.getPrice().multiply(BigDecimal.valueOf(t.getShares())), t.getCreatedAt());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(e.getMessage())));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> malformed(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }
}
