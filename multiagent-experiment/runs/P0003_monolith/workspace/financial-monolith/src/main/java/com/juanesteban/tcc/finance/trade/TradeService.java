package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.BusinessException;
import com.juanesteban.tcc.finance.portfolio.Holding;
import com.juanesteban.tcc.finance.portfolio.HoldingRepository;
import com.juanesteban.tcc.finance.quote.Quote;
import com.juanesteban.tcc.finance.quote.QuoteService;
import com.juanesteban.tcc.finance.user.UserAccount;
import com.juanesteban.tcc.finance.user.UserService;

@Service
public class TradeService {

    private final UserService userService;
    private final QuoteService quoteService;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;

    public TradeService(
        UserService userService,
        QuoteService quoteService,
        HoldingRepository holdingRepository,
        TradeRepository tradeRepository
    ) {
        this.userService = userService;
        this.quoteService = quoteService;
        this.holdingRepository = holdingRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public Trade buy(TradeRequest request) {
        Quote quote = quoteService.getQuote(request.symbol());
        long shares = request.shares();
        UserAccount user = userService.getUserForUpdate(request.userId());

        BigDecimal cost = quote.price().multiply(BigDecimal.valueOf(shares));
        if (user.getCashBalance().compareTo(cost) < 0) {
            throw new BusinessException("Insufficient cash balance");
        }

        Holding holding = holdingRepository
            .findByUserIdAndSymbol(user.getId(), quote.symbol())
            .orElseGet(() -> new Holding(user.getId(), quote.symbol()));
        holding.setShares(holding.getShares() + shares);
        holdingRepository.save(holding);

        user.setCashBalance(user.getCashBalance().subtract(cost));

        return tradeRepository.save(
            new Trade(user.getId(), quote.symbol(), TradeType.BUY, shares, quote.price()));
    }

    @Transactional
    public Trade sell(TradeRequest request) {
        Quote quote = quoteService.getQuote(request.symbol());
        long shares = request.shares();
        UserAccount user = userService.getUserForUpdate(request.userId());

        Holding holding = holdingRepository
            .findByUserIdAndSymbol(user.getId(), quote.symbol())
            .filter(h -> h.getShares() >= shares)
            .orElseThrow(() -> new BusinessException("Insufficient position"));

        long remaining = holding.getShares() - shares;
        if (remaining == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setShares(remaining);
        }

        BigDecimal proceeds = quote.price().multiply(BigDecimal.valueOf(shares));
        user.setCashBalance(user.getCashBalance().add(proceeds));

        return tradeRepository.save(
            new Trade(user.getId(), quote.symbol(), TradeType.SELL, shares, quote.price()));
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        userService.getUser(userId);
        return tradeRepository.findByUserIdOrderByExecutedAtAsc(userId);
    }
}
