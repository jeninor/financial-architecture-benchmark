package com.juanesteban.tcc.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FinanceController {
    private final FinanceService service;

    public FinanceController(FinanceService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView createUser(@Valid @RequestBody CreateUserRequest request) {
        UserAccount user = service.createUser(request.username());
        return new UserView(user.getId(), user.getUsername(), user.getCashBalance());
    }

    @GetMapping("/quotes/{symbol}")
    public QuoteView quote(@PathVariable String symbol) {
        return new QuoteView(symbol, service.price(symbol));
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public FinanceService.TradeView buy(@Valid @RequestBody TradeRequest request) {
        Trade trade = service.buy(request.userId(), request.symbol(), request.shares());
        return tradeView(trade);
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public FinanceService.TradeView sell(@Valid @RequestBody TradeRequest request) {
        Trade trade = service.sell(request.userId(), request.symbol(), request.shares());
        return tradeView(trade);
    }

    @GetMapping("/users/{userId}/portfolio")
    public FinanceService.Portfolio portfolio(@PathVariable UUID userId) {
        return service.portfolio(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<FinanceService.TradeView> history(@PathVariable UUID userId) {
        return service.history(userId);
    }

    private FinanceService.TradeView tradeView(Trade trade) {
        return new FinanceService.TradeView(trade.getId(), trade.getType(), trade.getSymbol(),
            trade.getShares(), trade.getPrice(), trade.getCreatedAt());
    }

    public record CreateUserRequest(@NotBlank String username) {}
    public record TradeRequest(@NotNull UUID userId, @NotBlank String symbol,
                               @Positive int shares) {}
    public record UserView(UUID id, String username, BigDecimal cashBalance) {}
    public record QuoteView(String symbol, BigDecimal price) {}
}
