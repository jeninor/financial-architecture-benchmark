package com.tcc.finance.trade.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento publicado na fila RabbitMQ "trade.audit.queue" ao concluir uma
 * compra ou venda. Usado apenas para auditoria/trilha assincrona — a
 * confirmacao da operacao ao usuario NAO depende da publicacao deste
 * evento (ver trade-off de consistencia eventual documentado no log do
 * Agente 1 para os microsservicos).
 */
public class TradeCompletedEvent implements Serializable {

    private String username;
    private String symbol;
    private String tipo;
    private long quantity;
    private BigDecimal preco;
    private Instant timestamp;

    public TradeCompletedEvent() {
    }

    public TradeCompletedEvent(String username, String symbol, String tipo, long quantity, BigDecimal preco, Instant timestamp) {
        this.username = username;
        this.symbol = symbol;
        this.tipo = tipo;
        this.quantity = quantity;
        this.preco = preco;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
