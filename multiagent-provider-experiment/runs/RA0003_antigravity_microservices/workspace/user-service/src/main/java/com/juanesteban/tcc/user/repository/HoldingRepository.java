package com.juanesteban.tcc.user.repository;

import com.juanesteban.tcc.user.model.HoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldingRepository extends JpaRepository<HoldingEntity, UUID> {
    List<HoldingEntity> findByUserId(UUID userId);
    Optional<HoldingEntity> findByUserIdAndSymbol(UUID userId, String symbol);
}
