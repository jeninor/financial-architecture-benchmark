package com.juanesteban.tcc.trade;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    private final TradeRepository trades;
    private final Clients.UserClient userClient;
    private final Clients.MarketClient marketClient;
    private final RabbitTemplate rabbit;

    public TradeController(TradeRepository trades, Clients.UserClient userClient,
                           Clients.MarketClient marketClient, RabbitTemplate rabbit) {
        this.trades = trades;
        this.userClient = userClient;
        this.marketClient = marketClient;
        this.rabbit = rabbit;
    }

    public record TradeRequest(String userId, String symbol, Integer shares) {}

    private static UUID parse(String s) {
        try {
            return s == null ? null : UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static ResponseEntity<Object> err(HttpStatus s, String m) {
        return ResponseEntity.status(s).body(Map.of("error", m));
    }

    /** Returns price, or null if symbol unknown. */
    private BigDecimal price(String symbol) {
        try {
            Object p = marketClient.quote(symbol).get("price");
            return new BigDecimal(p.toString());
        } catch (FeignException.BadRequest | FeignException.NotFound e) {
            return null;
        }
    }

    @PostMapping("/api/trades/buy")
    public ResponseEntity<Object> buy(@RequestBody TradeRequest r) {
        return trade("BUY", r);
    }

    @PostMapping("/api/trades/sell")
    public ResponseEntity<Object> sell(@RequestBody TradeRequest r) {
        return trade("SELL", r);
    }

    private ResponseEntity<Object> trade(String type, TradeRequest r) {
        if (r == null || r.shares() == null || r.shares() <= 0) {
            return err(HttpStatus.BAD_REQUEST, "shares must be positive");
        }
        UUID uid = parse(r.userId());
        if (uid == null) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        try {
            userClient.get(uid.toString());
        } catch (FeignException.NotFound e) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        if (r.symbol() == null) {
            return err(HttpStatus.BAD_REQUEST, "invalid symbol");
        }
        String symbol = r.symbol().toUpperCase();
        BigDecimal price = price(symbol);
        if (price == null) {
            return err(HttpStatus.BAD_REQUEST, "invalid symbol");
        }
        ResponseEntity<Object> rejection = applyToUser(uid, type, symbol, r.shares(), price);
        if (rejection != null) {
            return rejection;
        }
        Trade t = trades.save(new Trade(uid, type, symbol, r.shares(), price));
        publish(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(view(t));
    }

    /** Applies the trade in user-service; returns an error response, or null on success. */
    private ResponseEntity<Object> applyToUser(UUID uid, String type, String symbol,
                                               Integer shares, BigDecimal price) {
        try {
            userClient.apply(uid.toString(), Map.of(
                "type", type, "symbol", symbol, "shares", shares, "price", price));
            return null;
        } catch (FeignException.NotFound e) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        } catch (FeignException.BadRequest e) {
            return err(HttpStatus.BAD_REQUEST, "trade rejected");
        }
    }

    private void publish(Trade t) {
        try {
            rabbit.convertAndSend(MessagingConfig.EXCHANGE, MessagingConfig.KEY, view(t));
        } catch (Exception e) {
            log.warn("Could not publish trade.completed: {}", e.getMessage());
        }
    }

    private Map<String, Object> view(Trade t) {
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

    @GetMapping("/api/users/{userId}/trades")
    public ResponseEntity<Object> history(@PathVariable String userId) {
        UUID uid = parse(userId);
        if (uid == null) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        try {
            userClient.get(uid.toString());
        } catch (FeignException.NotFound e) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Trade t : trades.findByUserIdOrderByCreatedAtAsc(uid)) {
            out.add(view(t));
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/users/{userId}/portfolio")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> portfolio(@PathVariable String userId) {
        UUID uid = parse(userId);
        if (uid == null) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        Map<String, Object> user;
        try {
            user = userClient.get(uid.toString());
        } catch (FeignException.NotFound e) {
            return err(HttpStatus.NOT_FOUND, "user not found");
        }
        BigDecimal cash = new BigDecimal(user.get("cashBalance").toString());
        BigDecimal total = cash;
        List<Map<String, Object>> holdings = new ArrayList<>();
        for (Map<String, Object> h : (List<Map<String, Object>>) user.get("holdings")) {
            String symbol = h.get("symbol").toString();
            int shares = Integer.parseInt(h.get("shares").toString());
            BigDecimal p = price(symbol);
            if (p == null) {
                continue;
            }
            BigDecimal mv = p.multiply(BigDecimal.valueOf(shares));
            total = total.add(mv);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("symbol", symbol);
            m.put("shares", shares);
            m.put("price", p);
            m.put("marketValue", mv);
            holdings.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", uid);
        out.put("cash", cash);
        out.put("cashBalance", cash);
        out.put("holdings", holdings);
        out.put("totalValue", total);
        return ResponseEntity.ok(out);
    }
}
