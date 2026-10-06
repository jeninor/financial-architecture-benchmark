package com.juanesteban.tcc.trade;

import feign.FeignException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TradeController {
    private final TradeRepository trades;
    private final UserClient users;
    private final MarketClient market;
    private final RabbitTemplate rabbit;

    TradeController(TradeRepository trades, UserClient users, MarketClient market, RabbitTemplate rabbit) {
        this.trades = trades;
        this.users = users;
        this.market = market;
        this.rabbit = rabbit;
    }

    @PostMapping("/api/trades/buy")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse buy(@Valid @RequestBody TradeRequest request) {
        return execute("BUY", request);
    }

    @PostMapping("/api/trades/sell")
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse sell(@Valid @RequestBody TradeRequest request) {
        return execute("SELL", request);
    }

    @GetMapping("/api/users/{userId}/trades")
    public List<TradeResponse> history(@PathVariable UUID userId) {
        try {
            users.get(userId);
        } catch (FeignException ex) {
            throw mapped(ex);
        }
        return trades.findByUserIdOrderByCreatedAtAsc(userId).stream().map(this::response).toList();
    }

    private TradeResponse execute(String type, TradeRequest request) {
        try {
            users.get(request.userId());
        } catch (FeignException ex) {
            throw mapped(ex);
        }
        BigDecimal price;
        try {
            price = market.quote(request.symbol()).price();
        } catch (FeignException ex) {
            if (ex.status() == 400 || ex.status() == 404) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown symbol", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Market service failed", ex);
        }
        try {
            users.apply(request.userId(), new UserClient.TradeMutation(type, request.symbol(), request.shares()));
        } catch (FeignException ex) {
            throw mapped(ex);
        }
        TradeRecord record = trades.saveAndFlush(new TradeRecord(
                request.userId(), type, request.symbol(), request.shares(), price));
        try {
            rabbit.convertAndSend("financial.exchange", "trade.completed", record.id.toString());
        } catch (RuntimeException ignored) {
            // Audit delivery must not turn an already committed trade into an HTTP failure.
        }
        return response(record);
    }

    private ResponseStatusException mapped(FeignException ex) {
        if (ex.status() == 404) return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", ex);
        if (ex.status() == 400) return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade", ex);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Downstream service failed", ex);
    }

    private TradeResponse response(TradeRecord record) {
        return new TradeResponse(record.id, record.userId, record.type, record.symbol,
                record.shares, record.price, record.total, record.createdAt);
    }

    public record TradeRequest(@NotNull UUID userId, @NotBlank String symbol, @NotNull @Positive Integer shares) {}
    public record TradeResponse(UUID id, UUID userId, String type, String symbol, int shares,
                                BigDecimal price, BigDecimal total, Instant createdAt) {}
}
