package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.dto.*;
import com.juanesteban.tcc.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final QuoteService quoteService;

    public UserService(UserRepository userRepository, QuoteService quoteService) {
        this.userRepository = userRepository;
        this.quoteService = quoteService;
    }

    @Transactional
    public UserResponse createUser(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getCash());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PortfolioHolding> holdings = new ArrayList<>();
        BigDecimal totalValue = user.getCash();

        for (Map.Entry<String, Integer> entry : user.getPositions().entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            if (shares > 0) {
                BigDecimal price = quoteService.getQuote(symbol).orElse(BigDecimal.ZERO);
                BigDecimal marketValue = price.multiply(BigDecimal.valueOf(shares));
                holdings.add(new PortfolioHolding(symbol, shares, price, marketValue));
                totalValue = totalValue.add(marketValue);
            }
        }

        return new PortfolioResponse(user.getCash(), holdings, totalValue);
    }
}
