package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    public record CreateUserRequest(String username) {}

    public record UserResponse(UUID id, UUID userId, String username, BigDecimal cash, BigDecimal cashBalance) {}

    public record TradeApply(String symbol, long shares, BigDecimal price) {}

    public record HoldingView(String symbol, long shares, BigDecimal price, BigDecimal marketValue) {}

    public record PortfolioView(UUID userId, BigDecimal cash, BigDecimal cashBalance,
                                List<HoldingView> holdings, BigDecimal holdingsValue, BigDecimal totalValue) {}

    public record Quote(String symbol, BigDecimal price) {}

    @Configuration
    static class ClientConfig {
        @Bean
        @LoadBalanced
        RestClient.Builder loadBalancedRestClientBuilder() {
            return RestClient.builder();
        }
    }

    private final UserService service;
    private final RestClient market;

    public UserController(UserService service, RestClient.Builder builder) {
        this.service = service;
        this.market = builder.baseUrl("http://market-service").build();
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest req) {
        return view(service.create(req.username()));
    }

    @GetMapping("/api/users/{userId}/portfolio")
    public PortfolioView portfolio(@PathVariable String userId) {
        UserAccount u = service.get(parse(userId));
        List<HoldingView> holdings = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Position p : service.positions(u.getId())) {
            Quote q = market.get().uri("/api/quotes/{s}", p.getSymbol()).retrieve().body(Quote.class);
            BigDecimal value = q.price().multiply(BigDecimal.valueOf(p.getShares()));
            holdings.add(new HoldingView(p.getSymbol(), p.getShares(), q.price(), value));
            total = total.add(value);
        }
        return new PortfolioView(u.getId(), u.getCash(), u.getCash(), holdings, total, u.getCash().add(total));
    }

    // ---- internal endpoints used by trade-service ----

    @GetMapping("/internal/users/{userId}")
    public UserResponse get(@PathVariable String userId) {
        return view(service.get(parse(userId)));
    }

    @PostMapping("/internal/users/{userId}/buy")
    public UserResponse buy(@PathVariable String userId, @RequestBody TradeApply r) {
        return view(service.buy(parse(userId), r.symbol(), r.shares(), r.price()));
    }

    @PostMapping("/internal/users/{userId}/sell")
    public UserResponse sell(@PathVariable String userId, @RequestBody TradeApply r) {
        return view(service.sell(parse(userId), r.symbol(), r.shares(), r.price()));
    }

    private static UUID parse(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid user id");
        }
    }

    private static UserResponse view(UserAccount u) {
        return new UserResponse(u.getId(), u.getId(), u.getUsername(), u.getCash(), u.getCash());
    }
}
