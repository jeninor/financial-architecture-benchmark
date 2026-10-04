package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private BigDecimal cash = new BigDecimal("10000.00");

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_positions", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "symbol")
    @Column(name = "shares")
    private Map<String, Integer> positions = new HashMap<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public Map<String, Integer> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, Integer> positions) {
        this.positions = positions;
    }

    public void addCash(BigDecimal amount) {
        this.cash = this.cash.add(amount);
    }

    public void subtractCash(BigDecimal amount) {
        this.cash = this.cash.subtract(amount);
    }

    public void addShares(String symbol, int shares) {
        int current = this.positions.getOrDefault(symbol, 0);
        this.positions.put(symbol, current + shares);
    }

    public void subtractShares(String symbol, int shares) {
        int current = this.positions.getOrDefault(symbol, 0);
        int updated = current - shares;
        if (updated > 0) {
            this.positions.put(symbol, updated);
        } else {
            this.positions.remove(symbol);
        }
    }
    
    public int getShares(String symbol) {
        return this.positions.getOrDefault(symbol, 0);
    }
}
