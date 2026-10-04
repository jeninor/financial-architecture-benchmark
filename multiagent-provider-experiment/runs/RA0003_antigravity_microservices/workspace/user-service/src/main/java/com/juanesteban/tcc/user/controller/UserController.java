package com.juanesteban.tcc.user.controller;

import com.juanesteban.tcc.user.client.MarketClient;
import com.juanesteban.tcc.user.dto.*;
import com.juanesteban.tcc.user.model.HoldingEntity;
import com.juanesteban.tcc.user.model.UserEntity;
import com.juanesteban.tcc.user.repository.HoldingRepository;
import com.juanesteban.tcc.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final MarketClient marketClient;

    public UserController(UserRepository userRepository, HoldingRepository holdingRepository, MarketClient marketClient) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
        this.marketClient = marketClient;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() || userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(user.getId(), user.getUsername()));
    }

    @GetMapping("/{userId}/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntity user = userOpt.get();

        List<HoldingEntity> holdings = holdingRepository.findByUserId(userId);
        List<HoldingDto> holdingDtos = new ArrayList<>();
        BigDecimal totalValue = user.getCashBalance();

        for (HoldingEntity h : holdings) {
            MarketClient.QuoteResponse quote;
            try {
                quote = marketClient.getQuote(h.getSymbol());
            } catch (Exception e) {
                // If we can't get the quote, we might fail or default. Let's just return 400 or skip.
                return ResponseEntity.badRequest().build();
            }
            BigDecimal marketValue = quote.getPrice().multiply(BigDecimal.valueOf(h.getShares()));
            totalValue = totalValue.add(marketValue);
            holdingDtos.add(new HoldingDto(h.getSymbol(), h.getShares(), quote.getPrice(), marketValue));
        }

        PortfolioResponse response = new PortfolioResponse();
        response.setCashBalance(user.getCashBalance());
        response.setHoldings(holdingDtos);
        response.setTotalValue(totalValue);

        return ResponseEntity.ok(response);
    }
}
