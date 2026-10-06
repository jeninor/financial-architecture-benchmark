package com.juanesteban.tcc.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {
    private final UserAccountRepository accounts;
    private final MarketClient market;

    UserController(UserAccountRepository accounts, MarketClient market) {
        this.accounts = accounts;
        this.market = market;
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUser request) {
        if (accounts.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        try {
            UserAccount account = accounts.saveAndFlush(new UserAccount(request.username()));
            return new UserResponse(account.id, account.username, account.cashBalance);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists", ex);
        }
    }

    @GetMapping("/internal/users/{userId}")
    public UserResponse get(@PathVariable UUID userId) {
        UserAccount account = find(userId);
        return new UserResponse(account.id, account.username, account.cashBalance);
    }

    @GetMapping("/api/users/{userId}/portfolio")
    @Transactional(readOnly = true)
    public Portfolio portfolio(@PathVariable UUID userId) {
        UserAccount account = find(userId);
        List<Holding> holdings = new ArrayList<>();
        BigDecimal total = account.cashBalance;
        for (var entry : account.holdings.entrySet()) {
            BigDecimal price = market.quote(entry.getKey()).price();
            BigDecimal value = price.multiply(BigDecimal.valueOf(entry.getValue()));
            holdings.add(new Holding(entry.getKey(), entry.getValue(), price, value));
            total = total.add(value);
        }
        holdings.sort(Comparator.comparing(Holding::symbol));
        return new Portfolio(account.id, account.cashBalance, holdings, total);
    }

    @PostMapping("/internal/users/{userId}/trades")
    @Transactional
    public UserResponse apply(@PathVariable UUID userId, @Valid @RequestBody ApplyTrade request) {
        UserAccount account = accounts.findWithLockById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (request.shares() == null || request.shares() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shares must be positive");
        }
        BigDecimal price = market.quote(request.symbol()).price();
        BigDecimal amount = price.multiply(BigDecimal.valueOf(request.shares()));
        int owned = account.holdings.getOrDefault(request.symbol(), 0);
        if ("BUY".equals(request.type())) {
            if (account.cashBalance.compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient cash");
            }
            account.cashBalance = account.cashBalance.subtract(amount);
            account.holdings.put(request.symbol(), Math.addExact(owned, request.shares()));
        } else if ("SELL".equals(request.type())) {
            if (owned < request.shares()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient shares");
            }
            account.cashBalance = account.cashBalance.add(amount);
            if (owned == request.shares()) account.holdings.remove(request.symbol());
            else account.holdings.put(request.symbol(), owned - request.shares());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trade type");
        }
        return new UserResponse(account.id, account.username, account.cashBalance);
    }

    private UserAccount find(UUID id) {
        return accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public record CreateUser(@NotBlank String username) {}
    public record ApplyTrade(@NotBlank String type, @NotBlank String symbol, @NotNull Integer shares) {}
    public record UserResponse(UUID id, String username, BigDecimal cashBalance) {}
    public record Holding(String symbol, int shares, BigDecimal price, BigDecimal marketValue) {}
    public record Portfolio(UUID userId, BigDecimal cash, List<Holding> holdings, BigDecimal totalValue) {}
}
