package com.juanesteban.tcc.finance.repository;

import com.juanesteban.tcc.finance.domain.Position;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);

    List<Position> findByUserIdAndSharesGreaterThanOrderBySymbol(UUID userId, long shares);
}
