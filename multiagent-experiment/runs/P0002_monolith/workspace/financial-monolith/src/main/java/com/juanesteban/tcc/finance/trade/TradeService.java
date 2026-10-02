package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.BadRequestException;
import com.juanesteban.tcc.finance.portfolio.Holding;
import com.juanesteban.tcc.finance.portfolio.HoldingRepository;
import com.juanesteban.tcc.finance.quote.QuoteService;
import com.juanesteban.tcc.finance.user.AppUser;
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
    public Trade buy(UUID userId, String symbol, Integer shares) {
        validateShares(shares);
        QuoteService.Quote quote = quoteService.getQuote(symbol);
        AppUser user = userService.getUserForUpdate(userId);

        BigDecimal total = quote.price().multiply(BigDecimal.valueOf(shares));
        if (user.getCashBalance().compareTo(total) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, quote.symbol())
            .orElseGet(() -> new Holding(userId, quote.symbol()));
        holding.setShares(holding.getShares() + shares);
        user.setCashBalance(user.getCashBalance().subtract(total));

        holdingRepository.save(holding);
        return tradeRepository.save(new Trade(userId, TradeType.BUY, quote.symbol(), shares, quote.price(), total));
    }

    @Transactional
    public Trade sell(UUID userId, String symbol, Integer shares) {
        validateShares(shares);
        QuoteService.Quote quote = quoteService.getQuote(symbol);
        AppUser user = userService.getUserForUpdate(userId);

        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, quote.symbol())
            .filter(h -> h.getShares() >= shares)
            .orElseThrow(() -> new BadRequestException("Insufficient position"));

        BigDecimal total = quote.price().multiply(BigDecimal.valueOf(shares));
        holding.setShares(holding.getShares() - shares);
        user.setCashBalance(user.getCashBalance().add(total));

        holdingRepository.save(holding);
        return tradeRepository.save(new Trade(userId, TradeType.SELL, quote.symbol(), shares, quote.price(), total));
    }

    @Transactional(readOnly = true)
    public List<Trade> history(UUID userId) {
        userService.getUser(userId);
        return tradeRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    private void validateShares(Integer shares) {
        if (shares == null || shares <= 0) {
            throw new BadRequestException("Shares must be greater than zero");
        }
    }
}
