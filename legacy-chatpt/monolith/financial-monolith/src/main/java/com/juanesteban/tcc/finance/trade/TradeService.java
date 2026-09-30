package com.juanesteban.tcc.finance.trade;

import com.juanesteban.tcc.finance.market.Quote;
import com.juanesteban.tcc.finance.market.QuoteProvider;
import com.juanesteban.tcc.finance.user.User;
import com.juanesteban.tcc.finance.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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


    @Transactional
    public TradeResponse sell(
        SellRequest request
    ) {

        Quote quote =
            quoteProvider.getQuote(
                request.symbol()
            );


        int ownedShares =
            calculateOwnedShares(
                request.userId(),
                quote.symbol()
            );


        if (
            request.shares() > ownedShares
        ) {

            throw new IllegalArgumentException(
                "Insufficient shares. Available: "
                    + ownedShares
            );
        }


        BigDecimal total =
            quote
                .price()
                .multiply(
                    BigDecimal.valueOf(
                        request.shares()
                    )
                );


        User user =
            userService.credit(
                request.userId(),
                total
            );


        Trade trade =
            new Trade(
                request.userId(),
                quote.symbol(),
                TradeType.SELL,
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


    @Transactional(readOnly = true)
    public List<TradeHistoryResponse> getHistory(
        UUID userId
    ) {

        /*
         * Forces 404 if the user does not exist,
         * even if no trades exist.
         */
        userService.getById(userId);


        return tradeRepository
            .findByUserIdOrderByCreatedAtAsc(
                userId
            )
            .stream()
            .map(
                trade -> {

                    BigDecimal total =
                        trade
                            .getPrice()
                            .multiply(
                                BigDecimal.valueOf(
                                    trade.getShares()
                                )
                            );


                    return new TradeHistoryResponse(

                        trade.getId(),

                        trade.getSymbol(),

                        trade.getType(),

                        trade.getShares(),

                        trade.getPrice(),

                        total,

                        trade.getCreatedAt()
                    );
                }
            )
            .toList();
    }


    private int calculateOwnedShares(
        UUID userId,
        String symbol
    ) {

        return tradeRepository
            .findByUserIdOrderByCreatedAtAsc(
                userId
            )
            .stream()
            .filter(
                trade ->
                    trade
                        .getSymbol()
                        .equalsIgnoreCase(symbol)
            )
            .mapToInt(
                trade ->
                    trade.getType()
                        == TradeType.BUY
                        ? trade.getShares()
                        : -trade.getShares()
            )
            .sum();
    }
}