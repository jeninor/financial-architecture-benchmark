package com.tcc.finance.repository;

import com.tcc.finance.domain.TradeTransaction;
import com.tcc.finance.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, Long> {

    List<TradeTransaction> findByUserOrderByTimestampAscIdAsc(UserAccount user);
}
