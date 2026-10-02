package com.juanesteban.tcc.finance.user;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juanesteban.tcc.finance.common.BadRequestException;
import com.juanesteban.tcc.finance.common.NotFoundException;

@Service
public class UserService {

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("10000.00");

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser createUser(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank()) {
            throw new BadRequestException("Username is required");
        }
        String username = rawUsername.trim();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }
        try {
            return userRepository.saveAndFlush(new AppUser(username, INITIAL_BALANCE));
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Username already exists: " + username);
        }
    }

    @Transactional(readOnly = true)
    public AppUser getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Transactional
    public AppUser getUserForUpdate(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
