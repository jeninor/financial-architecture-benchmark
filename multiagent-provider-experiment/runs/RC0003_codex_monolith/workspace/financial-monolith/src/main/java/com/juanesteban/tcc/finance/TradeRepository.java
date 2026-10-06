package com.juanesteban.tcc.finance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
