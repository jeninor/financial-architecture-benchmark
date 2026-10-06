package com.juanesteban.tcc.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Configuration
class ClientConfig {
    @Bean
    @LoadBalanced
    RestClient.Builder lbRestClientBuilder() {
        return RestClient.builder();
    }
}

@RestController
public class UserController {

    private static final BigDecimal INITIAL_CASH = new BigDecimal("10000.00");

    private final UserRepository users;
    private final PositionRepository positions;
    private final RestClient market;

    @Autowired
    public UserController(UserRepository users, PositionRepository positions, RestClient.Builder builder) {
        this.users = users;
        this.positions = positions;
        this.market = builder.baseUrl("http://market-service").build();
    }

    @PostMapping("/api/users")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Object u = body == null ? null : body.get("username");
        if (!(u instanceof String username) || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username taken");
        }
        UserAccount account;
        try {
            account = users.saveAndFlush(new UserAccount(UUID.randomUUID(), username, INITIAL_CASH));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username taken");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", account.getId());
        out.put("userId", account.getId());
        out.put("username", account.getUsername());
        out.put("cash", account.getCash());
        out.put("cashBalance", account.getCash());
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @GetMapping("/api/users/{id}")
    public Map<String, Object> get(@PathVariable UUID id) {
        UserAccount a = users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.getId());
        out.put("userId", a.getId());
        out.put("username", a.getUsername());
        out.put("cash", a.getCash());
        return out;
    }

    @GetMapping("/api/users/{id}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID id) {
        UserAccount a = users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = a.getCash();
        for (Position p : positions.findByUserId(id)) {
            if (p.getShares() <= 0) continue;
            BigDecimal price = price(p.getSymbol());
            BigDecimal value = price.multiply(BigDecimal.valueOf(p.getShares()));
            total = total.add(value);
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("symbol", p.getSymbol());
            h.put("shares", p.getShares());
            h.put("price", price);
            h.put("marketValue", value);
            holdings.add(h);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", id);
        out.put("cash", a.getCash());
        out.put("cashBalance", a.getCash());
        out.put("holdings", holdings);
        out.put("totalValue", total);
        return out;
    }

    private BigDecimal price(String symbol) {
        Map<?, ?> q = market.get().uri("/api/quotes/{s}", symbol).retrieve().body(Map.class);
        return new BigDecimal(String.valueOf(q.get("price")));
    }

    /** Atomically applies a trade to cash and position; rejections change nothing. */
    @PostMapping("/internal/users/{id}/apply")
    @Transactional
    public Map<String, Object> apply(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String type = String.valueOf(body.get("type"));
        String symbol = String.valueOf(body.get("symbol"));
        long shares = ((Number) body.get("shares")).longValue();
        BigDecimal price = new BigDecimal(String.valueOf(body.get("price")));
        if (shares <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid shares");
        }
        UserAccount a = users.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal amount = price.multiply(BigDecimal.valueOf(shares));
        Optional<Position> pos = positions.findByUserIdAndSymbol(id, symbol);
        if ("BUY".equals(type)) {
            if (a.getCash().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
            }
            a.setCash(a.getCash().subtract(amount));
            Position p = pos.orElseGet(() -> new Position(id, symbol, 0));
            p.setShares(p.getShares() + shares);
            positions.save(p);
        } else if ("SELL".equals(type)) {
            if (pos.isEmpty() || pos.get().getShares() < shares) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient position");
            }
            a.setCash(a.getCash().add(amount));
            pos.get().setShares(pos.get().getShares() - shares);
            positions.save(pos.get());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid type");
        }
        users.save(a);
        return Map.of("cash", a.getCash());
    }
}
