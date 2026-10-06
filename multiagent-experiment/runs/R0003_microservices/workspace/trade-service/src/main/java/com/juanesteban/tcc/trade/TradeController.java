package com.juanesteban.tcc.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Configuration
class TradeConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder lbRestClientBuilder() {
        return RestClient.builder();
    }

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

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    private final TradeRepository trades;
    private final RabbitTemplate rabbit;
    private final RestClient market;
    private final RestClient users;

    public TradeController(TradeRepository trades, RabbitTemplate rabbit, RestClient.Builder builder) {
        this.trades = trades;
        this.rabbit = rabbit;
        this.market = builder.clone().baseUrl("http://market-service").build();
        this.users = builder.clone().baseUrl("http://user-service").build();
    }

    @PostMapping("/api/trades/buy")
    public ResponseEntity<Map<String, Object>> buy(@RequestBody Map<String, Object> body) {
        return trade("BUY", body);
    }

    @PostMapping("/api/trades/sell")
    public ResponseEntity<Map<String, Object>> sell(@RequestBody Map<String, Object> body) {
        return trade("SELL", body);
    }

    private ResponseEntity<Map<String, Object>> trade(String type, Map<String, Object> body) {
        UUID userId;
        try {
            userId = UUID.fromString(String.valueOf(body.get("userId")));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid userId");
        }
        Object s = body.get("shares");
        if (!(s instanceof Number n) || n.doubleValue() != Math.floor(n.doubleValue()) || n.longValue() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shares must be a positive integer");
        }
        long shares = n.longValue();
        Object sy = body.get("symbol");
        if (!(sy instanceof String symbol) || symbol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "symbol required");
        }
        symbol = symbol.toUpperCase();

        BigDecimal price;
        try {
            Map<?, ?> q = market.get().uri("/api/quotes/{s}", symbol).retrieve().body(Map.class);
            price = new BigDecimal(String.valueOf(q.get("price")));
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "quote failed");
        }

        Map<String, Object> apply = new HashMap<>();
        apply.put("type", type);
        apply.put("symbol", symbol);
        apply.put("shares", shares);
        apply.put("price", price);
        try {
            users.post().uri("/internal/users/{id}/apply", userId).body(apply).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "user-service rejected");
        }

        TradeRecord rec = trades.save(new TradeRecord(userId, type, symbol, shares, price));
        Map<String, Object> out = toMap(rec);
        try {
            rabbit.convertAndSend("financial.exchange", "trade.completed", out);
        } catch (Exception e) {
            log.warn("Could not publish trade.completed: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @GetMapping("/api/users/{id}/trades")
    public List<Map<String, Object>> history(@PathVariable UUID id) {
        try {
            users.get().uri("/api/users/{id}", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.getStatusCode().value()), "user lookup failed");
        }
        return trades.findByUserIdOrderByCreatedAtAsc(id).stream().map(TradeController::toMap).toList();
    }

    private static Map<String, Object> toMap(TradeRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("userId", r.getUserId());
        m.put("type", r.getType());
        m.put("symbol", r.getSymbol());
        m.put("shares", r.getShares());
        m.put("price", r.getPrice());
        m.put("total", r.getPrice().multiply(BigDecimal.valueOf(r.getShares())));
        m.put("createdAt", r.getCreatedAt().toString());
        return m;
    }
}
