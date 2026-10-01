package com.tcc.finance.repository;

import com.tcc.finance.model.Posicao;
import com.tcc.finance.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosicaoRepository extends JpaRepository<Posicao, Long> {

    Optional<Posicao> findByUsuarioAndSymbol(Usuario usuario, String symbol);

    List<Posicao> findByUsuario(Usuario usuario);
}
