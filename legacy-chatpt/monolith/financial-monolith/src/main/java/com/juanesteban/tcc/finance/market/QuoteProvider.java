package com.juanesteban.tcc.finance.market;

public interface QuoteProvider {

    Quote getQuote(String symbol);

}