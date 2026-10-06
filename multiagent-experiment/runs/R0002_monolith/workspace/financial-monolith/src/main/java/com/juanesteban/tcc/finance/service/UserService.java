package com.juanesteban.tcc.finance.service;

import com.juanesteban.tcc.finance.domain.User;
import com.juanesteban.tcc.finance.repository.UserRepository;
import java.math.BigDecimal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000.00");

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public User create(String username) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("username is required");
        }
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw new BadRequestException("username already exists");
        }
        try {
            return users.saveAndFlush(new User(name, INITIAL_BALANCE));
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("username already exists");
        }
    }
}
