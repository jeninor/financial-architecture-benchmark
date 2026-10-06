package com.juanesteban.tcc.finance;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findForUpdate(UUID id);
}

interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserIdAndSymbol(UUID userId, String symbol);

    List<Holding> findByUserIdOrderBySymbol(UUID userId);
}

interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserIdOrderByIdAsc(UUID userId);
}
