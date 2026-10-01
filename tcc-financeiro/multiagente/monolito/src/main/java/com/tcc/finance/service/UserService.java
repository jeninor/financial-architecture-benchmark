package com.tcc.finance.service;

import com.tcc.finance.domain.UserAccount;
import com.tcc.finance.exception.BadRequestException;
import com.tcc.finance.exception.ConflictException;
import com.tcc.finance.exception.NotFoundException;
import com.tcc.finance.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000.00");

    private final UserAccountRepository userRepository;

    public UserService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserAccount create(String username) {
        if (username == null || username.isBlank()) {
            throw new BadRequestException("username e obrigatorio");
        }
        String normalized = username.trim();
        if (userRepository.existsByUsername(normalized)) {
            throw new ConflictException("Username ja existe: " + normalized);
        }
        return userRepository.saveAndFlush(new UserAccount(normalized, INITIAL_BALANCE));
    }

    @Transactional(readOnly = true)
    public UserAccount get(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }
}
