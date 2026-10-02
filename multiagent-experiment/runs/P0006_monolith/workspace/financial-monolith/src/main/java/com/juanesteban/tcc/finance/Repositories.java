package com.juanesteban.tcc.finance;

import com.juanesteban.tcc.finance.Entities.*;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public final class Repositories {

    private Repositories() {}

    public interface UserRepository extends JpaRepository<AppUser, UUID> {
        boolean existsByUsername(String username);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select u from AppUser u where u.id = ?1")
        Optional<AppUser> findForUpdate(UUID id);
    }

    public interface PositionRepository extends JpaRepository<Position, UUID> {
        Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);
        List<Position> findByUserIdOrderBySymbol(UUID userId);
    }

    public interface TradeRepository extends JpaRepository<Trade, UUID> {
        List<Trade> findByUserIdOrderByCreatedAtAsc(UUID userId);
    }
}
