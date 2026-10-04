package com.juanesteban.tcc.finance;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public class Repos {

    public interface UserRepo extends JpaRepository<Domain.AppUser, UUID> {
        boolean existsByUsername(String username);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select u from Domain$AppUser u where u.id = ?1")
        Optional<Domain.AppUser> lockById(UUID id);
    }

    public interface PositionRepo extends JpaRepository<Domain.Position, Long> {
        Optional<Domain.Position> findByUserIdAndSymbol(UUID userId, String symbol);

        List<Domain.Position> findByUserId(UUID userId);
    }

    public interface TradeRepo extends JpaRepository<Domain.Trade, Long> {
        List<Domain.Trade> findByUserIdOrderByIdAsc(UUID userId);
    }
}
