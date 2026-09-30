package com.juanesteban.tcc.trade.trade;

import com.juanesteban.tcc.trade.client.BalanceChangeRequest;
import com.juanesteban.tcc.trade.client.BalanceResponse;
import com.juanesteban.tcc.trade.client.MarketClient;
import com.juanesteban.tcc.trade.client.MarketQuoteResponse;
import com.juanesteban.tcc.trade.client.UserClient;

import com.juanesteban.tcc.trade.messaging.TradeCompletedEvent;
import com.juanesteban.tcc.trade.messaging.TradeEventPublisher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    private final MarketClient marketClient;

    private final UserClient userClient;

    private final TradeEventPublisher tradeEventPublisher;


    public TradeService(
        TradeRepository tradeRepository,
        MarketClient marketClient,
        UserClient userClient,
        TradeEventPublisher tradeEventPublisher
    ) {

        this.tradeRepository =
            tradeRepository;

        this.marketClient =
            marketClient;

        this.userClient =
            userClient;

        this.tradeEventPublisher =
            tradeEventPublisher;
    }


    // =========================================================
    // BUY
    // =========================================================

    @Transactional
    public TradeResponse buy(
        BuyRequest request
    ) {

        /*
         * 1. Ask Market Service for the current stock price.
         */
        MarketQuoteResponse quote =
            marketClient.getQuote(
                request.symbol()
            );


        /*
         * 2. Calculate the total cost.
         *
         * Example:
         *
         * 10 AAPL × 200 = 2000
         */
        BigDecimal total =
            quote
                .price()
                .multiply(
                    BigDecimal.valueOf(
                        request.shares()
                    )
                );


        /*
         * 3. Ask User Service to debit the money.
         */
        BalanceResponse balance =
            userClient.debit(

                request.userId(),

                new BalanceChangeRequest(
                    total
                )
            );


        /*
         * 4. Create the local Trade entity.
         */
        Trade trade =
            new Trade(

                request.userId(),

                quote.symbol(),

                TradeType.BUY,

                request.shares(),

                quote.price()
            );


        /*
         * 5. Save the trade in trade_db.
         */
        Trade saved =
            tradeRepository.save(
                trade
            );


        /*
         * 6. Publish asynchronous event through RabbitMQ.
         */
        publishTradeCompleted(
            saved,
            total
        );


        /*
         * 7. Return response to client.
         */
        return response(

            saved,

            total,

            balance.cash()
        );
    }


    // =========================================================
    // SELL
    // =========================================================

    @Transactional
    public TradeResponse sell(
        SellRequest request
    ) {

        /*
         * 1. Obtain current price from Market Service.
         */
        MarketQuoteResponse quote =
            marketClient.getQuote(
                request.symbol()
            );


        /*
         * 2. Calculate how many shares the user currently owns.
         */
        int ownedShares =
            calculateOwnedShares(

                request.userId(),

                quote.symbol()
            );


        /*
         * 3. Prevent selling more shares than owned.
         */
        if (
            request.shares()
                > ownedShares
        ) {

            throw new IllegalArgumentException(

                "Insufficient shares. Available: "
                    + ownedShares
            );
        }


        /*
         * 4. Calculate sale value.
         */
        BigDecimal total =
            quote
                .price()
                .multiply(
                    BigDecimal.valueOf(
                        request.shares()
                    )
                );


        /*
         * 5. Ask User Service to credit the money.
         */
        BalanceResponse balance =
            userClient.credit(

                request.userId(),

                new BalanceChangeRequest(
                    total
                )
            );


        /*
         * 6. Register the SELL operation locally.
         */
        Trade trade =
            new Trade(

                request.userId(),

                quote.symbol(),

                TradeType.SELL,

                request.shares(),

                quote.price()
            );


        Trade saved =
            tradeRepository.save(
                trade
            );


        /*
         * 7. Publish asynchronous RabbitMQ event.
         */
        publishTradeCompleted(
            saved,
            total
        );


        /*
         * 8. Return result.
         */
        return response(

            saved,

            total,

            balance.cash()
        );
    }


    // =========================================================
    // HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<TradeHistoryResponse> getHistory(
        UUID userId
    ) {

        /*
         * Ask User Service to confirm that the user exists.
         *
         * If the user does not exist, Feign propagates
         * the corresponding HTTP error.
         */
        userClient.getUser(
            userId
        );


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


    // =========================================================
    // OWNED SHARES
    // =========================================================

    private int calculateOwnedShares(
        UUID userId,
        String symbol
    ) {

        return tradeRepository
            .findByUserIdOrderByCreatedAtAsc(
                userId
            )
            .stream()

            /*
             * Only trades for the requested stock.
             */
            .filter(

                trade ->

                    trade
                        .getSymbol()
                        .equalsIgnoreCase(
                            symbol
                        )
            )

            /*
             * BUY  -> + shares
             * SELL -> - shares
             */
            .mapToInt(

                trade ->

                    trade.getType()
                        == TradeType.BUY

                        ? trade.getShares()

                        : -trade.getShares()
            )

            .sum();
    }


    // =========================================================
    // RESPONSE
    // =========================================================

    private TradeResponse response(
        Trade trade,
        BigDecimal total,
        BigDecimal cashAfter
    ) {

        return new TradeResponse(

            trade.getId(),

            trade.getUserId(),

            trade.getSymbol(),

            trade.getType(),

            trade.getShares(),

            trade.getPrice(),

            total,

            cashAfter,

            trade.getCreatedAt()
        );
    }


    // =========================================================
    // RABBITMQ EVENT
    // =========================================================

    private void publishTradeCompleted(
        Trade trade,
        BigDecimal total
    ) {

        TradeCompletedEvent event =
            new TradeCompletedEvent(

                trade.getId(),

                trade.getUserId(),

                trade.getSymbol(),

                trade
                    .getType()
                    .name(),

                trade.getShares(),

                trade.getPrice(),

                total,

                trade.getCreatedAt()
            );


        tradeEventPublisher.publish(
            event
        );
    }
}