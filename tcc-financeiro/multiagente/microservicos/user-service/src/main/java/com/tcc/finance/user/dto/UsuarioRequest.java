package com.tcc.finance.user.dto;

public class UsuarioRequest {
    private String username;

    public UsuarioRequest() {
    }

    public UsuarioRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
