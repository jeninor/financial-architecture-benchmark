package com.juanesteban.tcc.finance.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(

    @NotBlank
    String username

) {
}