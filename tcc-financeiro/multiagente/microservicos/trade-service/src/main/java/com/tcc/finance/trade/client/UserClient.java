package com.tcc.finance.trade.client;

import com.tcc.finance.trade.client.dto.BalanceOperationRequest;
import com.tcc.finance.trade.client.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{username}")
    UserDto getUser(@PathVariable("username") String username);

    /** Debito com lock pessimista na linha do usuario (dentro do user-service). */
    @PostMapping("/users/{username}/debit")
    UserDto debit(@PathVariable("username") String username, @RequestBody BalanceOperationRequest request);

    /** Credito com lock pessimista na linha do usuario (dentro do user-service). */
    @PostMapping("/users/{username}/credit")
    UserDto credit(@PathVariable("username") String username, @RequestBody BalanceOperationRequest request);
}
