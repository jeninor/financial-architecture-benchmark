package com.juanesteban.tcc.finance;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

interface UserRepository extends JpaRepository<AppUser, UUID> {
    boolean existsByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from AppUser u where u.id = ?1")
    Optional<AppUser> findForUpdate(UUID id);
}

interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);

    List<Position> findByUserIdOrderBySymbol(UUID userId);
}

interface TradeRepository extends JpaRepository<TradeRecord, Long> {
    List<TradeRecord> findByUserIdOrderByIdAsc(UUID userId);
}
