package com.tcc.finance.user.service;

import com.tcc.finance.user.domain.UserAccount;
import com.tcc.finance.user.exception.BadRequestException;
import com.tcc.finance.user.exception.ConflictException;
import com.tcc.finance.user.exception.NotFoundException;
import com.tcc.finance.user.repository.UserAccountRepository;
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

    /**
     * Debita o saldo com lock pessimista na linha do usuario: requisicoes concorrentes
     * de compra (vindas do trade-service) sao serializadas e o saldo nunca fica negativo.
     */
    @Transactional
    public UserAccount debit(String username, BigDecimal amount) {
        validateAmount(amount);
        UserAccount user = lockUser(username);
        if (amount.compareTo(user.getSaldo()) > 0) {
            throw new BadRequestException("Saldo insuficiente: custo " + amount
                    + " excede o saldo disponivel " + user.getSaldo());
        }
        user.debit(amount);
        return user;
    }

    @Transactional
    public UserAccount credit(String username, BigDecimal amount) {
        validateAmount(amount);
        UserAccount user = lockUser(username);
        user.credit(amount);
        return user;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("amount deve ser um valor positivo (> 0)");
        }
    }

    private UserAccount lockUser(String username) {
        return userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }
}
