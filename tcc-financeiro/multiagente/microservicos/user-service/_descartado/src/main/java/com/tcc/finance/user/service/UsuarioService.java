package com.tcc.finance.user.service;

import com.tcc.finance.user.exception.DuplicateUsernameException;
import com.tcc.finance.user.exception.InsufficientFundsException;
import com.tcc.finance.user.exception.UserNotFoundException;
import com.tcc.finance.user.model.Usuario;
import com.tcc.finance.user.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario criar(String username) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(username);
        }
        return usuarioRepository.save(new Usuario(username, Usuario.SALDO_INICIAL));
    }

    @Transactional(readOnly = true)
    public Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    /**
     * Endpoint interno usado pelo trade-service (via OpenFeign) para debitar
     * o saldo ao concluir uma compra. Mantido idempotente/local a este
     * servico: o trade-service nao participa de uma transacao distribuida
     * (sem Saga/2PC - trade-off documentado nos logs do Agente 1).
     */
    @Transactional
    public Usuario debitar(String username, BigDecimal amount) {
        Usuario usuario = buscar(username);
        if (amount.compareTo(usuario.getSaldo()) > 0) {
            throw new InsufficientFundsException();
        }
        usuario.setSaldo(usuario.getSaldo().subtract(amount));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario creditar(String username, BigDecimal amount) {
        Usuario usuario = buscar(username);
        usuario.setSaldo(usuario.getSaldo().add(amount));
        return usuarioRepository.save(usuario);
    }
}
