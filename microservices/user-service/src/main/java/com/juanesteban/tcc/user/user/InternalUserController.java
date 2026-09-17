package com.juanesteban.tcc.user.user;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;


    public InternalUserController(
        UserService userService
    ) {

        this.userService =
            userService;
    }


    @GetMapping("/{userId}")
    public UserResponse getById(
        @PathVariable
        UUID userId
    ) {

        return UserResponse.from(
            userService.getById(
                userId
            )
        );
    }


    @PostMapping(
        "/{userId}/debit"
    )
    public BalanceResponse debit(
        @PathVariable
        UUID userId,

        @Valid
        @RequestBody
        BalanceChangeRequest request
    ) {

        return BalanceResponse.from(

            userService.debit(
                userId,
                request.amount()
            )
        );
    }


    @PostMapping(
        "/{userId}/credit"
    )
    public BalanceResponse credit(
        @PathVariable
        UUID userId,

        @Valid
        @RequestBody
        BalanceChangeRequest request
    ) {

        return BalanceResponse.from(

            userService.credit(
                userId,
                request.amount()
            )
        );
    }
}