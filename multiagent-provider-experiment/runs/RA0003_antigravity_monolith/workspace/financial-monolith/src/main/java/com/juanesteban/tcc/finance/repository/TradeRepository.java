package com.juanesteban.tcc.finance.repository;

import com.juanesteban.tcc.finance.domain.Trade;
import com.juanesteban.tcc.finance.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserOrderByTimestampAsc(User user);
}
