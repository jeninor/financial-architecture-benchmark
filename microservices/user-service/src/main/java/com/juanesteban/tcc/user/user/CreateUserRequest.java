package com.juanesteban.tcc.user.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(

    @NotBlank
    String username

) {
}