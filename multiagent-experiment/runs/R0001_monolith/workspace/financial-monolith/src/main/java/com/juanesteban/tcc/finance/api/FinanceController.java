package com.juanesteban.tcc.finance.api;

import com.juanesteban.tcc.finance.domain.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FinanceController {

    private final FinanceService service;
    private final QuoteService quotes;

    public FinanceController(FinanceService service, QuoteService quotes) {
        this.service = service;
        this.quotes = quotes;
    }

    public record CreateUserRequest(String username) {
    }

    public record UserResponse(UUID id, UUID userId, String username, BigDecimal cash, BigDecimal cashBalance) {
    }

    public record QuoteResponse(String symbol, BigDecimal price) {
    }

    public record TradeRequest(UUID userId, String symbol, Long shares) {
    }

    public record TradeResponse(UUID id, UUID userId, String symbol, String type, long shares,
                                BigDecimal price, BigDecimal total, java.time.Instant createdAt) {
        static TradeResponse of(Trade t) {
            return new TradeResponse(t.getId(), t.getUserId(), t.getSymbol(), t.getType().name(),
                    t.getShares(), t.getPrice(), t.getTotal(), t.getCreatedAt());
        }
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        AppUser u = service.createUser(request.username());
        return new UserResponse(u.getId(), u.getId(), u.getUsername(), u.getCashBalance(), u.getCashBalance());
    }

    @GetMapping("/quotes/{symbol}")
    public QuoteResponse quote(@PathVariable String symbol) {
        return new QuoteResponse(symbol, quotes.priceOf(symbol));
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@RequestBody TradeRequest r) {
        return TradeResponse.of(service.buy(r.userId(), r.symbol(), r.shares()));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@RequestBody TradeRequest r) {
        return TradeResponse.of(service.sell(r.userId(), r.symbol(), r.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public FinanceService.Portfolio portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<TradeResponse> trades(@PathVariable UUID userId) {
        return service.history(userId).stream().map(TradeResponse::of).toList();
    }
}
