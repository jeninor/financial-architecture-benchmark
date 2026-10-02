package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.createUser(request.username()));
    }

    public record CreateUserRequest(@NotBlank String username) {
    }

    public record UserResponse(
        UUID id,
        UUID userId,
        String username,
        BigDecimal cash,
        BigDecimal cashBalance,
        Instant createdAt
    ) {
        static UserResponse from(AppUser user) {
            return new UserResponse(
                user.getId(),
                user.getId(),
                user.getUsername(),
                user.getCashBalance(),
                user.getCashBalance(),
                user.getCreatedAt()
            );
        }
    }
}
