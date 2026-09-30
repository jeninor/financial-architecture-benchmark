package com.juanesteban.tcc.trade.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
    name = "user-service"
)
public interface UserClient {


    @GetMapping(
        "/internal/users/{userId}"
    )
    UserResponse getUser(

        @PathVariable("userId")
        UUID userId
    );


    @PostMapping(
        "/internal/users/{userId}/debit"
    )
    BalanceResponse debit(

        @PathVariable("userId")
        UUID userId,

        @RequestBody
        BalanceChangeRequest request
    );


    @PostMapping(
        "/internal/users/{userId}/credit"
    )
    BalanceResponse credit(

        @PathVariable("userId")
        UUID userId,

        @RequestBody
        BalanceChangeRequest request
    );
}