package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    public record TradeRequest(String userId, String symbol, Long shares) {}

    public record TradeView(UUID id, UUID userId, String type, String symbol, long shares,
                            BigDecimal price, BigDecimal total, Instant createdAt) {}

    private final Clients.MarketClient market;
    private final Clients.UserClient users;
    private final TradeRepository trades;
    private final RabbitTemplate rabbit;

    public TradeController(Clients.MarketClient market, Clients.UserClient users,
                           TradeRepository trades, RabbitTemplate rabbit) {
        this.market = market;
        this.users = users;
        this.trades = trades;
        this.rabbit = rabbit;
    }

    @PostMapping("/api/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeView buy(@RequestBody TradeRequest req) {
        return execute("BUY", req);
    }

    @PostMapping("/api/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeView sell(@RequestBody TradeRequest req) {
        return execute("SELL", req);
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeView> history(@PathVariable String userId) {
        UUID id = parseId(userId);
        users.get(id.toString());
        return trades.findByUserIdOrderByCreatedAtAsc(id).stream().map(TradeController::view).toList();
    }

    private TradeView execute(String type, TradeRequest req) {
        if (req == null || req.shares() == null || req.shares() <= 0
            || req.symbol() == null || req.symbol().isBlank() || req.userId() == null) {
            throw new BadRequest();
        }
        UUID userId = parseId(req.userId());
        String symbol = req.symbol().trim().toUpperCase();
        long shares = req.shares();

        Clients.Quote quote = market.quote(symbol);
        Clients.Apply apply = new Clients.Apply(symbol, shares, quote.price());
        if (type.equals("BUY")) {
            users.buy(userId.toString(), apply);
        } else {
            users.sell(userId.toString(), apply);
        }

        TradeRecord saved = trades.save(new TradeRecord(userId, type, symbol, shares, quote.price()));
        TradeView v = view(saved);
        try {
            rabbit.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, v);
        } catch (Exception e) {
            log.warn("Could not publish trade.completed event: {}", e.getMessage());
        }
        return v;
    }

    private static UUID parseId(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            throw new BadRequest();
        }
    }

    private static TradeView view(TradeRecord t) {
        return new TradeView(t.getId(), t.getUserId(), t.getType(), t.getSymbol(), t.getShares(),
            t.getPrice(), t.getTotal(), t.getCreatedAt());
    }

    static class BadRequest extends RuntimeException {}

    @ExceptionHandler(BadRequest.class)
    ResponseEntity<Map<String, String>> badRequest() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Map<String, String>> downstream(FeignException e) {
        int s = e.status();
        HttpStatus status = (s == 400 || s == 404) ? HttpStatus.valueOf(s) : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of("error", "downstream " + s));
    }
}
