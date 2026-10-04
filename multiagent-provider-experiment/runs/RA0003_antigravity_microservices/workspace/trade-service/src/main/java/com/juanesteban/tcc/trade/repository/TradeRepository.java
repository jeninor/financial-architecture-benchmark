package com.juanesteban.tcc.trade.repository;

import com.juanesteban.tcc.trade.model.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {
    List<TradeEntity> findByUserId(UUID userId);
}
