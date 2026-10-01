package com.tcc.finance.trade.repository;

import com.tcc.finance.trade.domain.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction> findByUsernameOrderByTimestampAscIdAsc(String username);
}
