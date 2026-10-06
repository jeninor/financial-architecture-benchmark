package com.juanesteban.tcc.trade;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    private UUID id = UUID.randomUUID();
    private UUID userId;
    private String type;
    private String symbol;
    private long shares;
    private BigDecimal price;
    private BigDecimal total;
    private Instant createdAt = Instant.now();

    protected Trade() {
    }

    public Trade(UUID userId, String type, String symbol, long shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
}
