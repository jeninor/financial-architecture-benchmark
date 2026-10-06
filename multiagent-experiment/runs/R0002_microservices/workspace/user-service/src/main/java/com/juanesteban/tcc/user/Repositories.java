package com.juanesteban.tcc.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    boolean existsByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findForUpdate(@Param("id") UUID id);
}

interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByUserId(UUID userId);

    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);
}
