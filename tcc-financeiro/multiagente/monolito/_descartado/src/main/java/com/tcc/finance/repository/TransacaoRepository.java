package com.tcc.finance.repository;

import com.tcc.finance.model.Transacao;
import com.tcc.finance.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuarioOrderByTimestampAsc(Usuario usuario);
}
