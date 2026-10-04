package com.juanesteban.tcc.user.dto;

import java.util.UUID;

public class CreateUserResponse {
    private UUID id;
    private UUID userId;
    private String username;

    public CreateUserResponse(UUID id, String username) {
        this.id = id;
        this.userId = id;
        this.username = username;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
}
