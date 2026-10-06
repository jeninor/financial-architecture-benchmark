package com.juanesteban.tcc.trade;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Configuration
class MessagingConfig {

    @Bean
    DirectExchange financialExchange() {
        return new DirectExchange("financial.exchange");
    }

    @Bean
    Queue tradeAuditQueue() {
        return new Queue("trade.audit.queue", true);
    }

    @Bean
    Binding tradeAuditBinding(Queue tradeAuditQueue, DirectExchange financialExchange) {
        return BindingBuilder.bind(tradeAuditQueue).to(financialExchange).with("trade.completed");
    }
}

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    public record TradeRequest(UUID userId, String symbol, Integer shares) {}

    private final TradeRepository trades;
    private final MarketClient market;
    private final UserClient users;
    private final RabbitTemplate rabbit;

    public TradeController(TradeRepository trades, MarketClient market, UserClient users, RabbitTemplate rabbit) {
        this.trades = trades;
        this.market = market;
        this.users = users;
        this.rabbit = rabbit;
    }

    @PostMapping("/api/trades/buy")
    public ResponseEntity<Map<String, Object>> buy(@RequestBody TradeRequest req) {
        return execute("BUY", req);
    }

    @PostMapping("/api/trades/sell")
    public ResponseEntity<Map<String, Object>> sell(@RequestBody TradeRequest req) {
        return execute("SELL", req);
    }

    @GetMapping("/api/users/{id}/trades")
    public List<Map<String, Object>> history(@PathVariable UUID id) {
        checkUser(id);
        return trades.findByUserIdOrderByCreatedAtAsc(id).stream().map(TradeController::view).toList();
    }

    private ResponseEntity<Map<String, Object>> execute(String type, TradeRequest req) {
        if (req == null || req.userId() == null || req.symbol() == null || req.symbol().isBlank()
            || req.shares() == null || req.shares() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }
        String symbol = req.symbol().trim().toUpperCase();
        checkUser(req.userId());
        BigDecimal price;
        try {
            price = new BigDecimal(market.quote(symbol).get("price").toString());
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid symbol");
        }
        BigDecimal amount = price.multiply(BigDecimal.valueOf(req.shares()));
        try {
            users.apply(req.userId(), new UserClient.Apply(type, symbol, req.shares(), amount));
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
            }
            if (e.status() == 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trade rejected");
            }
            throw e;
        }
        Trade t = trades.save(new Trade(req.userId(), type, symbol, req.shares(), price));
        try {
            rabbit.convertAndSend("financial.exchange", "trade.completed",
                "{\"tradeId\":\"" + t.getId() + "\",\"userId\":\"" + t.getUserId() + "\",\"type\":\"" + type
                    + "\",\"symbol\":\"" + symbol + "\",\"shares\":" + t.getShares() + "}");
        } catch (Exception e) {
            log.warn("Could not publish trade.completed: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(view(t));
    }

    private void checkUser(UUID id) {
        try {
            users.get(id);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
            }
            throw e;
        }
    }

    private static Map<String, Object> view(Trade t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("userId", t.getUserId());
        m.put("type", t.getType());
        m.put("symbol", t.getSymbol());
        m.put("shares", t.getShares());
        m.put("price", t.getPrice());
        m.put("total", t.getTotal());
        m.put("createdAt", t.getCreatedAt());
        return m;
    }
}
