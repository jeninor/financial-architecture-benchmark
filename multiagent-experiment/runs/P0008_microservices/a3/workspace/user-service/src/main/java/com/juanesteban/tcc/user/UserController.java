package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    public record CreateUserRequest(String username) {
    }

    public record UserResponse(UUID id, UUID userId, String username, BigDecimal cashBalance) {
    }

    public record HoldingState(String symbol, int shares) {
    }

    public record UserState(UUID id, String username, BigDecimal cashBalance, List<HoldingState> holdings) {
    }

    public record ApplyRequest(String type, String symbol, Integer shares, BigDecimal price) {
    }

    private final UserRepository users;
    private final PositionRepository positions;

    public UserController(UserRepository users, PositionRepository positions) {
        this.users = users;
        this.positions = positions;
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest request) {
        String username = request == null || request.username() == null ? "" : request.username().trim();
        if (username.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username is required");
        }
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        try {
            UserAccount u = users.saveAndFlush(new UserAccount(username, new BigDecimal("10000.00")));
            return new UserResponse(u.getId(), u.getId(), u.getUsername(), u.getCashBalance());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    @GetMapping("/internal/users/{id}")
    @Transactional(readOnly = true)
    public UserState get(@PathVariable("id") UUID id) {
        UserAccount u = users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return state(u);
    }

    @PostMapping("/internal/users/{id}/apply")
    @Transactional
    public UserState apply(@PathVariable("id") UUID id, @RequestBody ApplyRequest req) {
        validate(req);
        UserAccount u = users.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        BigDecimal total = req.price().multiply(BigDecimal.valueOf(req.shares()));
        Position pos = positions.findByUserIdAndSymbol(id, req.symbol()).orElse(null);

        if ("BUY".equals(req.type())) {
            applyBuy(u, pos, req, total);
        } else if ("SELL".equals(req.type())) {
            applySell(u, pos, req, total);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid type");
        }
        positions.flush();
        return state(u);
    }

    private void validate(ApplyRequest req) {
        if (req == null || req.shares() == null || req.shares() <= 0 || req.price() == null
            || req.symbol() == null || req.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
        }
    }

    private void applyBuy(UserAccount u, Position pos, ApplyRequest req, BigDecimal total) {
        if (u.getCashBalance().compareTo(total) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
        }
        u.setCashBalance(u.getCashBalance().subtract(total));
        if (pos == null) {
            positions.save(new Position(u.getId(), req.symbol(), req.shares()));
        } else {
            pos.setShares(pos.getShares() + req.shares());
        }
    }

    private void applySell(UserAccount u, Position pos, ApplyRequest req, BigDecimal total) {
        if (pos == null || pos.getShares() < req.shares()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient position");
        }
        u.setCashBalance(u.getCashBalance().add(total));
        pos.setShares(pos.getShares() - req.shares());
    }

    private UserState state(UserAccount u) {
        List<HoldingState> holdings = positions.findByUserId(u.getId()).stream()
            .filter(p -> p.getShares() > 0)
            .sorted(Comparator.comparing(Position::getSymbol))
            .map(p -> new HoldingState(p.getSymbol(), p.getShares()))
            .toList();
        return new UserState(u.getId(), u.getUsername(), u.getCashBalance(), holdings);
    }
}
