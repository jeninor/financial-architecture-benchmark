package com.juanesteban.tcc.user.user;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;


    public UserController(
        UserService userService
    ) {

        this.userService =
            userService;
    }


    @PostMapping
    @ResponseStatus(
        HttpStatus.CREATED
    )
    public UserResponse create(
        @Valid
        @RequestBody
        CreateUserRequest request
    ) {

        return UserResponse.from(
            userService.create(
                request.username()
            )
        );
    }
}