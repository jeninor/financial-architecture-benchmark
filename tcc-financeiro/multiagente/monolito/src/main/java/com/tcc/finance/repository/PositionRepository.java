package com.tcc.finance.repository;

import com.tcc.finance.domain.Position;
import com.tcc.finance.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByUserAndSymbol(UserAccount user, String symbol);

    List<Position> findByUserOrderBySymbolAsc(UserAccount user);
}
