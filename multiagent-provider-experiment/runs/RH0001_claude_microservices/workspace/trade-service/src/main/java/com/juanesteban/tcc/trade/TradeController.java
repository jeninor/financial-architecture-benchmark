package com.juanesteban.tcc.trade;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    public record TradeRequest(UUID userId, String symbol, Long shares) {}
    public record TradeView(UUID id, UUID userId, String type, String symbol, long shares,
                            BigDecimal price, BigDecimal total, java.time.Instant createdAt) {}
    public record HoldingView(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {}
    public record Portfolio(UUID userId, String username, BigDecimal cash, BigDecimal cashBalance,
                            List<HoldingView> holdings, BigDecimal totalValue) {}

    private final TradeRepository trades;
    private final MarketClient market;
    private final UserClient users;
    private final RabbitTemplate rabbit;

    public TradeController(TradeRepository trades, MarketClient market, UserClient users,
                           RabbitTemplate rabbit) {
        this.trades = trades;
        this.market = market;
        this.users = users;
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

    private TradeView execute(String type, TradeRequest req) {
        if (req == null || req.userId() == null || req.symbol() == null || req.symbol().isBlank()
                || req.shares() == null || req.shares() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }
        String symbol = req.symbol().trim().toUpperCase();
        BigDecimal price = price(symbol);
        BigDecimal total = price.multiply(BigDecimal.valueOf(req.shares()));
        try {
            users.apply(req.userId(), new UserClient.Apply(type, symbol, req.shares(), total));
        } catch (FeignException e) {
            throw map(e);
        }
        Trade t = trades.save(new Trade(req.userId(), type, symbol, req.shares(), price, total));
        TradeView v = view(t);
        try {
            rabbit.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.KEY,
                "{\"id\":\"" + t.getId() + "\",\"userId\":\"" + t.getUserId() + "\",\"type\":\""
                    + type + "\",\"symbol\":\"" + symbol + "\",\"shares\":" + t.getShares() + "}");
        } catch (Exception e) {
            log.warn("Could not publish trade.completed: {}", e.getMessage());
        }
        return v;
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeView> history(@PathVariable("userId") UUID userId) {
        try {
            users.get(userId);
        } catch (FeignException e) {
            throw map(e);
        }
        return trades.findByUserIdOrderByCreatedAtAsc(userId).stream().map(this::view).toList();
    }

    @GetMapping("/api/users/{userId}/portfolio")
    public Portfolio portfolio(@PathVariable("userId") UUID userId) {
        UserClient.UserView u;
        try {
            u = users.get(userId);
        } catch (FeignException e) {
            throw map(e);
        }
        BigDecimal total = u.cashBalance();
        List<HoldingView> hs = new ArrayList<>();
        for (UserClient.Holding h : u.holdings()) {
            BigDecimal p = price(h.symbol());
            BigDecimal mv = p.multiply(BigDecimal.valueOf(h.shares()));
            total = total.add(mv);
            hs.add(new HoldingView(h.symbol(), h.shares(), p, mv));
        }
        return new Portfolio(userId, u.username(), u.cashBalance(), u.cashBalance(), hs, total);
    }

    private BigDecimal price(String symbol) {
        try {
            return market.quote(symbol).price();
        } catch (FeignException e) {
            throw map(e);
        }
    }

    private ResponseStatusException map(FeignException e) {
        int s = e.status();
        if (s == 404) return new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        if (s == 400) return new ResponseStatusException(HttpStatus.BAD_REQUEST, "rejected");
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "downstream error");
    }

    private TradeView view(Trade t) {
        return new TradeView(t.getId(), t.getUserId(), t.getType(), t.getSymbol(), t.getShares(),
            t.getPrice(), t.getTotal(), t.getCreatedAt());
    }
}
