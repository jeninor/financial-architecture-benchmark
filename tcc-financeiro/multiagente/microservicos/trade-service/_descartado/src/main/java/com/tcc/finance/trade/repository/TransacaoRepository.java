package com.tcc.finance.trade.repository;

import com.tcc.finance.trade.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsernameOrderByTimestampAsc(String username);
}
