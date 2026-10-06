package com.juanesteban.tcc.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<TradeRecord, UUID> {

    List<TradeRecord> findByUserIdOrderByCreatedAtAsc(UUID userId);
}
