package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.dto.TradeHistoryItem;
import com.juanesteban.tcc.finance.repository.TradeRepository;
import com.juanesteban.tcc.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final QuoteService quoteService;

    public TradeService(TradeRepository tradeRepository, UserRepository userRepository, QuoteService quoteService) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
        this.quoteService = quoteService;
    }

    @Transactional
    public void buy(UUID userId, String symbol, int shares) {
        if (shares <= 0) {
            throw new IllegalArgumentException("Shares must be positive");
        }
        BigDecimal price = quoteService.getQuote(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Invalid symbol"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(shares));
        if (user.getCash().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient cash");
        }

        user.subtractCash(totalCost);
        user.addShares(symbol, shares);
        userRepository.save(user);

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol);
        trade.setShares(shares);
        trade.setPrice(price);
        trade.setType("BUY");
        tradeRepository.save(trade);
    }

    @Transactional
    public void sell(UUID userId, String symbol, int shares) {
        if (shares <= 0) {
            throw new IllegalArgumentException("Shares must be positive");
        }
        BigDecimal price = quoteService.getQuote(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Invalid symbol"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getShares(symbol) < shares) {
            throw new IllegalArgumentException("Insufficient position");
        }

        BigDecimal totalRevenue = price.multiply(BigDecimal.valueOf(shares));
        user.addCash(totalRevenue);
        user.subtractShares(symbol, shares);
        userRepository.save(user);

        Trade trade = new Trade();
        trade.setUser(user);
        trade.setSymbol(symbol);
        trade.setShares(shares);
        trade.setPrice(price);
        trade.setType("SELL");
        tradeRepository.save(trade);
    }

    @Transactional(readOnly = true)
    public List<TradeHistoryItem> getTrades(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tradeRepository.findByUserOrderByTimestampAsc(user).stream()
                .map(t -> new TradeHistoryItem(t.getType(), t.getSymbol(), t.getShares(), t.getPrice(), t.getTimestamp()))
                .collect(Collectors.toList());
    }
}
