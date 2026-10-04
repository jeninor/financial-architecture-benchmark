package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public
interface UserClient {
    record Holding(String symbol, long shares) {}
    record UserView(UUID id, String username, BigDecimal cashBalance, List<Holding> holdings) {}
    record Apply(String type, String symbol, Long shares, BigDecimal amount) {}

    @GetMapping("/internal/users/{id}")
    UserView get(@PathVariable("id") UUID id);

    @PostMapping("/internal/users/{id}/apply")
    UserView apply(@PathVariable("id") UUID id, @RequestBody Apply req);
}
