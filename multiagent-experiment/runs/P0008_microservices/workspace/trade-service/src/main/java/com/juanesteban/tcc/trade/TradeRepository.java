package com.juanesteban.tcc.trade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<TradeRecord, UUID> {

    List<TradeRecord> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
