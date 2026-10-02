package com.juanesteban.tcc.finance.web;

import com.juanesteban.tcc.finance.domain.AppUser;
import com.juanesteban.tcc.finance.domain.Position;
import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.service.QuoteService;
import com.juanesteban.tcc.finance.service.TradingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    public record CreateUserRequest(String username) {}

    public record TradeRequest(UUID userId, String symbol, Long shares) {}

    public record UserResponse(UUID id, UUID userId, String username, BigDecimal cash, BigDecimal cashBalance) {}

    public record QuoteResponse(String symbol, BigDecimal price) {}

    public record TradeResponse(UUID id, UUID userId, String symbol, String type, long shares,
                                BigDecimal price, BigDecimal total, Instant createdAt) {}

    private final TradingService service;
    private final QuoteService quotes;

    public ApiController(TradingService service, QuoteService quotes) {
        this.service = service;
        this.quotes = quotes;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody CreateUserRequest req) {
        AppUser u = service.createUser(req == null ? null : req.username());
        return new UserResponse(u.getId(), u.getId(), u.getUsername(), u.getCash(), u.getCash());
    }

    @GetMapping("/quotes/{symbol}")
    public QuoteResponse quote(@PathVariable String symbol) {
        String s = symbol.toUpperCase();
        return new QuoteResponse(s, quotes.priceOf(s));
    }

    @PostMapping("/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@RequestBody TradeRequest r) {
        return toResponse(service.buy(r.userId(), r.symbol(), r.shares()));
    }

    @PostMapping("/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@RequestBody TradeRequest r) {
        return toResponse(service.sell(r.userId(), r.symbol(), r.shares()));
    }

    @GetMapping("/users/{userId}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID userId) {
        AppUser u = service.getUser(userId);
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = u.getCash();
        for (Position p : service.holdings(userId)) {
            BigDecimal price = quotes.priceOf(p.getSymbol());
            BigDecimal mv = price.multiply(BigDecimal.valueOf(p.getShares()));
            total = total.add(mv);
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("symbol", p.getSymbol());
            h.put("shares", p.getShares());
            h.put("price", price);
            h.put("marketValue", mv);
            holdings.add(h);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", u.getId());
        out.put("cash", u.getCash());
        out.put("cashBalance", u.getCash());
        out.put("holdings", holdings);
        out.put("totalValue", total);
        return out;
    }

    @GetMapping("/users/{userId}/trades")
    public List<TradeResponse> trades(@PathVariable UUID userId) {
        return service.history(userId).stream().map(this::toResponse).toList();
    }

    private TradeResponse toResponse(Trade t) {
        return new TradeResponse(t.getId(), t.getUserId(), t.getSymbol(), t.getType(),
                t.getShares(), t.getPrice(), t.getTotal(), t.getCreatedAt());
    }
}
