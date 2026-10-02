package com.tcc.finance.service;

import com.tcc.finance.exception.DuplicateUsernameException;
import com.tcc.finance.exception.UserNotFoundException;
import com.tcc.finance.model.Usuario;
import com.tcc.finance.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Usuario usuario = new Usuario(username, Usuario.SALDO_INICIAL);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscar(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}
