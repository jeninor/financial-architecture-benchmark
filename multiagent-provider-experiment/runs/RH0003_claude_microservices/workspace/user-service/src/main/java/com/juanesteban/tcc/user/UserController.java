package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

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

    public record CreateUser(String username) {}

    public record Apply(String type, String symbol, long shares, BigDecimal amount) {}

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/api/users")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateUser req) {
        UserAccount u = service.create(req.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(view(u));
    }

    @GetMapping("/api/users/{id}/portfolio")
    public Map<String, Object> portfolio(@PathVariable UUID id) {
        return service.portfolio(id);
    }

    @GetMapping("/internal/users/{id}")
    public Map<String, Object> get(@PathVariable UUID id) {
        return view(service.get(id));
    }

    @PostMapping("/internal/users/{id}/apply")
    public Map<String, Object> apply(@PathVariable UUID id, @RequestBody Apply req) {
        return service.apply(id, req.type(), req.symbol(), req.shares(), req.amount());
    }

    private static Map<String, Object> view(UserAccount u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("userId", u.getId());
        m.put("username", u.getUsername());
        m.put("cash", u.getCash());
        m.put("cashBalance", u.getCash());
        return m;
    }
}
