package com.tcc.finance.trade.repository;

import com.tcc.finance.trade.domain.Position;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Position p where p.username = :username and p.symbol = :symbol")
    Optional<Position> findByUsernameAndSymbolForUpdate(@Param("username") String username,
                                                        @Param("symbol") String symbol);

    List<Position> findByUsernameOrderBySymbolAsc(String username);
}
