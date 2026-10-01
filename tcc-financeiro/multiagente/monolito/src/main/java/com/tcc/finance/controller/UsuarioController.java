package com.tcc.finance.controller;

import com.tcc.finance.dto.UsuarioRequest;
import com.tcc.finance.dto.UsuarioResponse;
import com.tcc.finance.model.Usuario;
import com.tcc.finance.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.criar(request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioResponse(usuario.getUsername(), usuario.getSaldo()));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable String username) {
        Usuario usuario = usuarioService.buscar(username);
        return ResponseEntity.ok(new UsuarioResponse(usuario.getUsername(), usuario.getSaldo()));
    }
}
