package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id
    public UUID id;
    @Column(nullable = false, unique = true)
    public String username;
    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal cash;
}
