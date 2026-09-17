package com.juanesteban.tcc.finance.user;

import com.juanesteban.tcc.finance.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;


    public UserService(
        UserRepository userRepository
    ) {

        this.userRepository =
            userRepository;
    }


    @Transactional
    public User create(
        String username
    ) {

        if (
            userRepository
                .existsByUsername(username)
        ) {

            throw new IllegalArgumentException(
                "Username already exists"
            );
        }


        User user =
            new User(username);


        return userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public User getById(
        UUID userId
    ) {

        return userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found: "
                            + userId
                    )
            );
    }


    @Transactional
    public User debit(
        UUID userId,
        BigDecimal amount
    ) {

        User user =
            getById(userId);


        if (
            user
                .getCash()
                .compareTo(amount) < 0
        ) {

            throw new IllegalArgumentException(
                "Insufficient funds"
            );
        }


        user.setCash(
            user
                .getCash()
                .subtract(amount)
        );


        return userRepository.save(user);
    }

    @Transactional
    public User credit(
        UUID userId,
        BigDecimal amount
    ) {

        User user =
            getById(userId);


        user.setCash(
            user
                .getCash()
                .add(amount)
        );


        return userRepository.save(user);
    }
}