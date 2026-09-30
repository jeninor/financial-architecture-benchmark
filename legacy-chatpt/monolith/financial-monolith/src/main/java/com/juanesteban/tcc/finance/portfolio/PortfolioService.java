package com.juanesteban.tcc.finance.portfolio;

import com.juanesteban.tcc.finance.market.Quote;
import com.juanesteban.tcc.finance.market.QuoteProvider;
import com.juanesteban.tcc.finance.trade.Trade;
import com.juanesteban.tcc.finance.trade.TradeRepository;
import com.juanesteban.tcc.finance.trade.TradeType;
import com.juanesteban.tcc.finance.user.User;
import com.juanesteban.tcc.finance.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PortfolioService {

    private final UserService userService;

    private final TradeRepository tradeRepository;

    private final QuoteProvider quoteProvider;


    public PortfolioService(
        UserService userService,
        TradeRepository tradeRepository,
        QuoteProvider quoteProvider
    ) {

        this.userService =
            userService;

        this.tradeRepository =
            tradeRepository;

        this.quoteProvider =
            quoteProvider;
    }


    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(
        UUID userId
    ) {

        User user =
            userService.getById(userId);


        List<Trade> trades =
            tradeRepository
                .findByUserIdOrderByCreatedAtAsc(
                    userId
                );


        Map<String, Integer> sharesBySymbol =
            new HashMap<>();


        for (Trade trade : trades) {

            int change =
                trade.getType()
                    == TradeType.BUY
                    ? trade.getShares()
                    : -trade.getShares();


            sharesBySymbol.merge(
                trade.getSymbol(),
                change,
                Integer::sum
            );
        }


        List<HoldingResponse> holdings =
            new ArrayList<>();


        BigDecimal holdingsValue =
            BigDecimal.ZERO;


        for (
            Map.Entry<String, Integer> entry
                : sharesBySymbol.entrySet()
        ) {

            if (entry.getValue() <= 0) {
                continue;
            }


            Quote quote =
                quoteProvider.getQuote(
                    entry.getKey()
                );


            BigDecimal value =
                quote
                    .price()
                    .multiply(
                        BigDecimal.valueOf(
                            entry.getValue()
                        )
                    );


            holdings.add(
                new HoldingResponse(
                    entry.getKey(),
                    entry.getValue(),
                    quote.price(),
                    value
                )
            );


            holdingsValue =
                holdingsValue.add(value);
        }


        holdings.sort(
            Comparator.comparing(
                HoldingResponse::symbol
            )
        );


        BigDecimal totalValue =
            user
                .getCash()
                .add(holdingsValue);


        return new PortfolioResponse(
            userId,
            user.getCash(),
            holdings,
            holdingsValue,
            totalValue
        );
    }
}