package com.juanesteban.tcc.user;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findForUpdate(@Param("id") UUID id);
}

interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByUserId(UUID userId);
    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);
}
