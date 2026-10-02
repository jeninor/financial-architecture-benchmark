package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.BusinessException;
import com.juanesteban.tcc.finance.common.NotFoundException;

@Service
public class UserService {

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000.00");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserAccount createUser(String username) {
        String normalized = username.trim();
        if (userRepository.existsByUsername(normalized)) {
            throw new BusinessException("Username already exists: " + normalized);
        }
        try {
            return userRepository.saveAndFlush(new UserAccount(normalized, INITIAL_BALANCE));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Username already exists: " + normalized);
        }
    }

    @Transactional(readOnly = true)
    public UserAccount getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserAccount getUserForUpdate(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
