package com.juanesteban.tcc.trade.portfolio;

import com.juanesteban.tcc.trade.client.*;
import com.juanesteban.tcc.trade.trade.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PortfolioService {

    private final UserClient userClient;

    private final MarketClient marketClient;

    private final TradeRepository tradeRepository;


    public PortfolioService(
        UserClient userClient,
        MarketClient marketClient,
        TradeRepository tradeRepository
    ) {

        this.userClient =
            userClient;

        this.marketClient =
            marketClient;

        this.tradeRepository =
            tradeRepository;
    }


    public PortfolioResponse getPortfolio(
        UUID userId
    ) {

        UserResponse user =
            userClient.getUser(
                userId
            );


        List<Trade> trades =
            tradeRepository
                .findByUserIdOrderByCreatedAtAsc(
                    userId
                );


        Map<String, Integer>
            sharesBySymbol =
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


        List<HoldingResponse>
            holdings =
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


            MarketQuoteResponse quote =
                marketClient.getQuote(
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
                holdingsValue.add(
                    value
                );
        }


        holdings.sort(
            Comparator.comparing(
                HoldingResponse::symbol
            )
        );


        return new PortfolioResponse(

            userId,

            user.cash(),

            holdings,

            holdingsValue,

            user
                .cash()
                .add(
                    holdingsValue
                )
        );
    }
}