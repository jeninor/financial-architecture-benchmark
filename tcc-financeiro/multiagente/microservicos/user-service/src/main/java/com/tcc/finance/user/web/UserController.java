package com.tcc.finance.user.web;

import com.tcc.finance.user.domain.UserAccount;
import com.tcc.finance.user.service.UserService;
import com.tcc.finance.user.web.dto.BalanceOperationRequest;
import com.tcc.finance.user.web.dto.CreateUserRequest;
import com.tcc.finance.user.web.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest request) {
        return toResponse(userService.create(request.username()));
    }

    @GetMapping("/{username}")
    public UserResponse get(@PathVariable String username) {
        return toResponse(userService.get(username));
    }

    /** Uso interno: chamado pelo trade-service (OpenFeign) durante uma compra. */
    @PostMapping("/{username}/debit")
    public UserResponse debit(@PathVariable String username, @RequestBody BalanceOperationRequest request) {
        return toResponse(userService.debit(username, request.amount()));
    }

    /** Uso interno: chamado pelo trade-service (OpenFeign) durante uma venda. */
    @PostMapping("/{username}/credit")
    public UserResponse credit(@PathVariable String username, @RequestBody BalanceOperationRequest request) {
        return toResponse(userService.credit(username, request.amount()));
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getSaldo());
    }
}
