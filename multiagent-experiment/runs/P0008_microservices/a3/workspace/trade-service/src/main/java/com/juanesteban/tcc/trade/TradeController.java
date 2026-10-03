package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.juanesteban.tcc.trade.Clients.ApplyRequest;
import com.juanesteban.tcc.trade.Clients.MarketClient;
import com.juanesteban.tcc.trade.Clients.Quote;
import com.juanesteban.tcc.trade.Clients.UserClient;
import com.juanesteban.tcc.trade.Clients.UserState;

import feign.FeignException;

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    public record TradeRequest(UUID userId, String symbol, Integer shares) {
    }

    public record TradeResponse(UUID id, UUID userId, String symbol, String type, int shares,
                                BigDecimal price, BigDecimal total, Instant createdAt) {
    }

    public record HoldingView(String symbol, int shares, BigDecimal price, BigDecimal marketValue) {
    }

    public record PortfolioResponse(UUID userId, BigDecimal cash, BigDecimal cashBalance,
                                    List<HoldingView> holdings, BigDecimal holdingsValue,
                                    BigDecimal totalValue) {
    }

    private final TradeRepository trades;
    private final UserClient userClient;
    private final MarketClient marketClient;
    private final RabbitTemplate rabbit;

    public TradeController(TradeRepository trades, UserClient userClient,
                           MarketClient marketClient, RabbitTemplate rabbit) {
        this.trades = trades;
        this.userClient = userClient;
        this.marketClient = marketClient;
        this.rabbit = rabbit;
    }

    @PostMapping("/api/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@RequestBody TradeRequest request) {
        return execute("BUY", request);
    }

    @PostMapping("/api/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@RequestBody TradeRequest request) {
        return execute("SELL", request);
    }

    private TradeResponse execute(String type, TradeRequest req) {
        if (req == null || req.userId() == null || req.symbol() == null
            || req.shares() == null || req.shares() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid trade request");
        }
        Quote quote = quote(req.symbol());
        try {
            userClient.apply(req.userId(), new ApplyRequest(type, quote.symbol(), req.shares(), quote.price()));
        } catch (FeignException e) {
            throw translate(e);
        }
        TradeRecord saved = trades.save(
            new TradeRecord(req.userId(), quote.symbol(), type, req.shares(), quote.price()));
        publish(saved);
        return view(saved);
    }

    private void publish(TradeRecord t) {
        try {
            String payload = String.format(
                "{\"tradeId\":\"%s\",\"userId\":\"%s\",\"symbol\":\"%s\",\"type\":\"%s\",\"shares\":%d,\"price\":%s}",
                t.getId(), t.getUserId(), t.getSymbol(), t.getType(), t.getShares(), t.getPrice().toPlainString());
            rabbit.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.ROUTING_KEY, payload);
        } catch (Exception e) {
            log.warn("Could not publish trade.completed event: {}", e.getMessage());
        }
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeResponse> history(@PathVariable("userId") UUID userId) {
        fetchUser(userId);
        return trades.findByUserIdOrderByCreatedAtAsc(userId).stream().map(this::view).toList();
    }

    @GetMapping("/api/users/{userId}/portfolio")
    public PortfolioResponse portfolio(@PathVariable("userId") UUID userId) {
        UserState user = fetchUser(userId);
        BigDecimal holdingsValue = BigDecimal.ZERO;
        List<HoldingView> views = new java.util.ArrayList<>();
        for (Clients.HoldingState h : user.holdings()) {
            BigDecimal price = quote(h.symbol()).price();
            BigDecimal value = price.multiply(BigDecimal.valueOf(h.shares()));
            holdingsValue = holdingsValue.add(value);
            views.add(new HoldingView(h.symbol(), h.shares(), price, value));
        }
        BigDecimal cash = user.cashBalance();
        return new PortfolioResponse(userId, cash, cash, views, holdingsValue, cash.add(holdingsValue));
    }

    private UserState fetchUser(UUID id) {
        return withRetry(() -> userClient.get(id));
    }

    private Quote quote(String symbol) {
        return withRetry(() -> marketClient.quote(symbol));
    }

    // Idempotent reads only: tolerate Eureka/load-balancer propagation delays.
    private <T> T withRetry(java.util.function.Supplier<T> call) {
        FeignException last = null;
        for (int i = 0; i < 8; i++) {
            try {
                return call.get();
            } catch (FeignException e) {
                if (e.status() == 400 || e.status() == 404) {
                    throw translate(e);
                }
                last = e;
                log.warn("Downstream call failed (attempt {}): {}", i + 1, e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw translate(last);
    }

    private ResponseStatusException translate(FeignException e) {
        int s = e == null ? -1 : e.status();
        if (s == 400 || s == 404) {
            return new ResponseStatusException(HttpStatus.valueOf(s), "downstream rejected request");
        }
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "downstream service error");
    }

    private TradeResponse view(TradeRecord t) {
        return new TradeResponse(t.getId(), t.getUserId(), t.getSymbol(), t.getType(), t.getShares(),
            t.getPrice(), t.getTotal(), t.getCreatedAt());
    }
}
