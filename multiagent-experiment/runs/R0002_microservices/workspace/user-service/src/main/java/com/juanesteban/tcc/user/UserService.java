package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private static final BigDecimal INITIAL_CASH = new BigDecimal("10000.00");

    private final UserAccountRepository users;
    private final PositionRepository positions;

    public UserService(UserAccountRepository users, PositionRepository positions) {
        this.users = users;
        this.positions = positions;
    }

    @Transactional
    public UserAccount create(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        return users.saveAndFlush(new UserAccount(UUID.randomUUID(), name, INITIAL_CASH));
    }

    @Transactional(readOnly = true)
    public UserAccount get(UUID id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    @Transactional(readOnly = true)
    public List<Position> positions(UUID id) {
        return positions.findByUserId(id).stream().filter(p -> p.getShares() > 0).toList();
    }

    /** Applies a validated BUY atomically: cash check, debit and position increase. */
    @Transactional
    public UserAccount buy(UUID id, String symbol, long shares, BigDecimal price) {
        UserAccount u = lock(id);
        BigDecimal cost = price.multiply(BigDecimal.valueOf(shares));
        if (u.getCash().compareTo(cost) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
        }
        u.setCash(u.getCash().subtract(cost));
        Position p = positions.findByUserIdAndSymbol(id, symbol)
            .orElseGet(() -> new Position(id, symbol, 0));
        p.setShares(p.getShares() + shares);
        positions.save(p);
        return users.save(u);
    }

    /** Applies a validated SELL atomically: position check, decrease and credit. */
    @Transactional
    public UserAccount sell(UUID id, String symbol, long shares, BigDecimal price) {
        UserAccount u = lock(id);
        Position p = positions.findByUserIdAndSymbol(id, symbol).orElse(null);
        if (p == null || p.getShares() < shares) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient position");
        }
        p.setShares(p.getShares() - shares);
        positions.save(p);
        u.setCash(u.getCash().add(price.multiply(BigDecimal.valueOf(shares))));
        return users.save(u);
    }

    private UserAccount lock(UUID id) {
        return users.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }
}
