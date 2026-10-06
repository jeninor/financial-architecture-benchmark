package com.juanesteban.tcc.finance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    Optional<Holding> findByUserIdAndSymbol(UUID userId, String symbol);

    List<Holding> findByUserIdOrderBySymbol(UUID userId);
}
