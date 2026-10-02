package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.ApiException;

@Service
public class UserService {

    public static final BigDecimal INITIAL_CASH = new BigDecimal("10000.00");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserAccount create(String rawUsername) {
        String username = rawUsername.trim();
        if (userRepository.existsByUsername(username)) {
            throw ApiException.badRequest("Username already exists: " + username);
        }
        try {
            return userRepository.saveAndFlush(new UserAccount(username, INITIAL_CASH));
        } catch (DataIntegrityViolationException e) {
            throw ApiException.badRequest("Username already exists: " + username);
        }
    }

    @Transactional(readOnly = true)
    public UserAccount get(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> ApiException.notFound("User not found: " + userId));
    }

    @Transactional
    public UserAccount getForUpdate(Long userId) {
        return userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> ApiException.notFound("User not found: " + userId));
    }
}
