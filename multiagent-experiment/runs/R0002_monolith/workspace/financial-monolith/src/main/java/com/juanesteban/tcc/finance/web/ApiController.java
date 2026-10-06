package com.juanesteban.tcc.finance.web;

import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.service.BadRequestException;
import com.juanesteban.tcc.finance.service.PortfolioService;
import com.juanesteban.tcc.finance.service.QuoteService;
import com.juanesteban.tcc.finance.service.TradeService;
import com.juanesteban.tcc.finance.service.UserService;
import com.juanesteban.tcc.finance.web.Dtos.*;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserService userService;
    private final QuoteService quotes;
    private final TradeService tradeService;
    private final PortfolioService portfolioService;

    public ApiController(UserService userService, QuoteService quotes,
                         TradeService tradeService, PortfolioService portfolioService) {
        this.userService = userService;
        this.quotes = quotes;
        this.tradeService = tradeService;
        this.portfolioService = portfolioService;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User u = userService.create(request.username());
        return new UserResponse(u.getId(), u.getId(), u.getUsername(), u.getCashBalance(), u.getCashBalance());
    }

    @GetMapping("/quotes/{symbol}")
    public QuoteResponse quote(@PathVariable String symbol) {
        return new QuoteResponse(quotes.normalize(symbol), quotes.priceOf(symbol));
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@RequestBody TradeRequest request) {
        validate(request);
        return toResponse(tradeService.buy(request.userId(), request.symbol(), request.shares()));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@RequestBody TradeRequest request) {
        validate(request);
        return toResponse(tradeService.sell(request.userId(), request.symbol(), request.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public PortfolioResponse portfolio(@PathVariable UUID userId) {
        return portfolioService.get(userId);
    }

    @GetMapping("/users/{userId}/trades")
    public List<TradeResponse> trades(@PathVariable UUID userId) {
        return tradeService.history(userId).stream().map(ApiController::toResponse).toList();
    }

    private static void validate(TradeRequest r) {
        if (r.userId() == null || r.symbol() == null || r.shares() == null) {
            throw new BadRequestException("userId, symbol and shares are required");
        }
    }

    private static TradeResponse toResponse(Trade t) {
        return new TradeResponse(t.getId(), t.getUserId(), t.getSymbol(), t.getType().name(),
                t.getShares(), t.getPrice(), t.getTotal(), t.getCreatedAt());
    }
}
