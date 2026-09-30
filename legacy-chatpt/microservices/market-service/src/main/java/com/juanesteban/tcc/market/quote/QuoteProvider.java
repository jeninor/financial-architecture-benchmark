package com.juanesteban.tcc.market.quote;

public interface QuoteProvider {

    Quote getQuote(
        String symbol
    );
}