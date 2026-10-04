package com.juanesteban.tcc.trade;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
