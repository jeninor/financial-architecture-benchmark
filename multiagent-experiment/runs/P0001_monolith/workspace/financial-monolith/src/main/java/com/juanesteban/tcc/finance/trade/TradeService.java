package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.ApiException;
import com.juanesteban.tcc.finance.portfolio.Holding;
import com.juanesteban.tcc.finance.portfolio.HoldingRepository;
import com.juanesteban.tcc.finance.quote.QuoteResponse;
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
        // The user row lock serializes concurrent trades of the same user.
        UserAccount user = userService.getForUpdate(request.userId());
        QuoteResponse quote = resolveQuote(request.symbol());
        long shares = request.shares();

        BigDecimal total = totalFor(quote.price(), shares);
        if (user.getCashBalance().compareTo(total) < 0) {
            throw ApiException.badRequest("Insufficient funds");
        }

        user.setCashBalance(user.getCashBalance().subtract(total));

        Holding holding = holdingRepository.findByUserIdAndSymbol(user.getId(), quote.symbol())
            .orElseGet(() -> new Holding(user.getId(), quote.symbol(), 0));
        holding.setShares(holding.getShares() + shares);
        holdingRepository.save(holding);

        return tradeRepository.save(new Trade(user.getId(), quote.symbol(), TradeSide.BUY, shares, quote.price(), total));
    }

    @Transactional
    public Trade sell(TradeRequest request) {
        UserAccount user = userService.getForUpdate(request.userId());
        QuoteResponse quote = resolveQuote(request.symbol());
        long shares = request.shares();

        Holding holding = holdingRepository.findByUserIdAndSymbol(user.getId(), quote.symbol())
            .orElse(null);
        if (holding == null || holding.getShares() < shares) {
            throw ApiException.badRequest("Insufficient shares");
        }

        BigDecimal total = totalFor(quote.price(), shares);
        user.setCashBalance(user.getCashBalance().add(total));

        long remaining = holding.getShares() - shares;
        if (remaining == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setShares(remaining);
            holdingRepository.save(holding);
        }

        return tradeRepository.save(new Trade(user.getId(), quote.symbol(), TradeSide.SELL, shares, quote.price(), total));
    }

    @Transactional(readOnly = true)
    public List<Trade> history(Long userId) {
        userService.get(userId);
        return tradeRepository.findByUserIdOrderByIdAsc(userId);
    }

    private QuoteResponse resolveQuote(String symbol) {
        return quoteService.find(symbol)
            .orElseThrow(() -> ApiException.badRequest("Unknown symbol: " + symbol));
    }

    private static BigDecimal totalFor(BigDecimal price, long shares) {
        return price.multiply(BigDecimal.valueOf(shares)).setScale(2, RoundingMode.HALF_UP);
    }
}
