package com.juanesteban.tcc.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByUserId(UUID userId);

    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);
}
