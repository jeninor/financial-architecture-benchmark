package com.juanesteban.tcc.market.quote;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Service
public class MockQuoteProvider
        implements QuoteProvider {

    private static final Map<String, Quote>
        QUOTES =
            Map.of(

                "AAPL",
                new Quote(
                    "AAPL",
                    "Apple Inc.",
                    new BigDecimal("200.00")
                ),

                "MSFT",
                new Quote(
                    "MSFT",
                    "Microsoft Corporation",
                    new BigDecimal("400.00")
                ),

                "GOOGL",
                new Quote(
                    "GOOGL",
                    "Alphabet Inc.",
                    new BigDecimal("170.00")
                ),

                "AMZN",
                new Quote(
                    "AMZN",
                    "Amazon.com Inc.",
                    new BigDecimal("190.00")
                ),

                "NVDA",
                new Quote(
                    "NVDA",
                    "NVIDIA Corporation",
                    new BigDecimal("120.00")
                )
            );


    @Override
    public Quote getQuote(
        String symbol
    ) {

        if (
            symbol == null ||
            symbol.isBlank()
        ) {

            throw new IllegalArgumentException(
                "Stock symbol is required"
            );
        }


        String normalized =
            symbol
                .trim()
                .toUpperCase(
                    Locale.ROOT
                );


        Quote quote =
            QUOTES.get(
                normalized
            );


        if (quote == null) {

            throw new IllegalArgumentException(
                "Invalid stock symbol: "
                    + normalized
            );
        }


        return quote;
    }
}