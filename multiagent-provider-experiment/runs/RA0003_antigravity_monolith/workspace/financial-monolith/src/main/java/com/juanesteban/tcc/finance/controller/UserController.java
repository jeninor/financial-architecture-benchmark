package com.juanesteban.tcc.finance.controller;

import com.juanesteban.tcc.finance.dto.PortfolioResponse;
import com.juanesteban.tcc.finance.dto.TradeHistoryItem;
import com.juanesteban.tcc.finance.dto.UserRequest;
import com.juanesteban.tcc.finance.dto.UserResponse;
import com.juanesteban.tcc.finance.service.TradeService;
import com.juanesteban.tcc.finance.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final TradeService tradeService;

    public UserController(UserService userService, TradeService tradeService) {
        this.userService = userService;
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}/portfolio")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID userId) {
        PortfolioResponse response = userService.getPortfolio(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/trades")
    public ResponseEntity<List<TradeHistoryItem>> getTrades(@PathVariable UUID userId) {
        List<TradeHistoryItem> response = tradeService.getTrades(userId);
        return ResponseEntity.ok(response);
    }
}
