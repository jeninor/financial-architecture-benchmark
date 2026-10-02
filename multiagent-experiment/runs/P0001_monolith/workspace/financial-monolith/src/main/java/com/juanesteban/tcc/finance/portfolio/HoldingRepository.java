package com.juanesteban.tcc.finance.portfolio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserIdAndSymbol(Long userId, String symbol);

    List<Holding> findByUserIdOrderBySymbolAsc(Long userId);
}
