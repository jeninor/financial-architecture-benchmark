package com.tcc.finance.trade.repository;

import com.tcc.finance.trade.model.Posicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosicaoRepository extends JpaRepository<Posicao, Long> {

    Optional<Posicao> findByUsernameAndSymbol(String username, String symbol);

    List<Posicao> findByUsername(String username);
}
