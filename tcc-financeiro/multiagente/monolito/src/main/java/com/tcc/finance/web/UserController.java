package com.tcc.finance.web;

import com.tcc.finance.domain.UserAccount;
import com.tcc.finance.service.UserService;
import com.tcc.finance.web.dto.CreateUserRequest;
import com.tcc.finance.web.dto.UserResponse;
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

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getSaldo());
    }
}
