package com.juanesteban.tcc.finance.trade;

import com.juanesteban.tcc.finance.market.Quote;
import com.juanesteban.tcc.finance.market.QuoteProvider;
import com.juanesteban.tcc.finance.user.User;
import com.juanesteban.tcc.finance.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    private final QuoteProvider quoteProvider;

    private final UserService userService;


    public TradeService(
        TradeRepository tradeRepository,
        QuoteProvider quoteProvider,
        UserService userService
    ) {

        this.tradeRepository =
            tradeRepository;

        this.quoteProvider =
            quoteProvider;

        this.userService =
            userService;
    }


    @Transactional
    public TradeResponse buy(
        BuyRequest request
    ) {

        Quote quote =
            quoteProvider.getQuote(
                request.symbol()
            );


        BigDecimal total =
            quote
                .price()
                .multiply(
                    BigDecimal.valueOf(
                        request.shares()
                    )
                );


        User user =
            userService.debit(
                request.userId(),
                total
            );


        Trade trade =
            new Trade(
                request.userId(),
                quote.symbol(),
                TradeType.BUY,
                request.shares(),
                quote.price()
            );


        Trade savedTrade =
            tradeRepository.save(trade);


        return new TradeResponse(

            savedTrade.getId(),

            savedTrade.getUserId(),

            savedTrade.getSymbol(),

            savedTrade.getType(),

            savedTrade.getShares(),

            savedTrade.getPrice(),

            total,

            user.getCash(),

            savedTrade.getCreatedAt()
        );
    }
}