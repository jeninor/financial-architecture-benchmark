package com.juanesteban.tcc.user;

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
    @Query("select a from UserAccount a where a.id = :id")
    Optional<UserAccount> findWithLockById(@Param("id") UUID id);
}
