package com.tcc.finance.user.controller;

import com.tcc.finance.user.dto.AmountRequest;
import com.tcc.finance.user.dto.UsuarioRequest;
import com.tcc.finance.user.dto.UsuarioResponse;
import com.tcc.finance.user.model.Usuario;
import com.tcc.finance.user.service.UsuarioService;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(usuario));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable String username) {
        return ResponseEntity.ok(toResponse(usuarioService.buscar(username)));
    }

    /**
     * Endpoint interno consumido pelo trade-service via OpenFeign ao
     * concluir uma compra.
     */
    @PostMapping("/{username}/debit")
    public ResponseEntity<UsuarioResponse> debitar(@PathVariable String username, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(toResponse(usuarioService.debitar(username, request.getAmount())));
    }

    /**
     * Endpoint interno consumido pelo trade-service via OpenFeign ao
     * concluir uma venda.
     */
    @PostMapping("/{username}/credit")
    public ResponseEntity<UsuarioResponse> creditar(@PathVariable String username, @RequestBody AmountRequest request) {
        return ResponseEntity.ok(toResponse(usuarioService.creditar(username, request.getAmount())));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getUsername(), usuario.getSaldo());
    }
}
