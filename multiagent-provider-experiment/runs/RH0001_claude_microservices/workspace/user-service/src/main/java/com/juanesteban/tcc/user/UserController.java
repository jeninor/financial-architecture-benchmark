package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    public record CreateUser(String username) {}
    public record Holding(String symbol, long shares) {}
    public record UserView(UUID id, UUID userId, String username, BigDecimal cashBalance,
                           BigDecimal cash, List<Holding> holdings) {}
    public record Apply(String type, String symbol, Long shares, BigDecimal amount) {}

    private final UserRepository users;
    private final PositionRepository positions;

    public UserController(UserRepository users, PositionRepository positions) {
        this.users = users;
        this.positions = positions;
    }

    @PostMapping("/api/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView create(@RequestBody CreateUser req) {
        String name = req == null || req.username() == null ? "" : req.username().trim();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }
        if (users.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        try {
            return view(users.saveAndFlush(new User(name)), List.of());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    @GetMapping("/internal/users/{id}")
    @Transactional(readOnly = true)
    public UserView get(@PathVariable("id") UUID id) {
        User u = users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        return view(u, holdings(id));
    }

    @PostMapping("/internal/users/{id}/apply")
    @Transactional
    public UserView apply(@PathVariable("id") UUID id, @RequestBody Apply req) {
        User u = users.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        long shares = req.shares() == null ? 0 : req.shares();
        if (shares <= 0 || req.amount() == null || req.symbol() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid operation");
        }
        Optional<Position> pos = positions.findByUserIdAndSymbol(id, req.symbol());
        if ("BUY".equals(req.type())) {
            if (u.getCashBalance().compareTo(req.amount()) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
            }
            u.setCashBalance(u.getCashBalance().subtract(req.amount()));
            if (pos.isPresent()) {
                pos.get().setShares(pos.get().getShares() + shares);
            } else {
                positions.save(new Position(id, req.symbol(), shares));
            }
        } else if ("SELL".equals(req.type())) {
            if (pos.isEmpty() || pos.get().getShares() < shares) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient position");
            }
            u.setCashBalance(u.getCashBalance().add(req.amount()));
            pos.get().setShares(pos.get().getShares() - shares);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid type");
        }
        users.save(u);
        return view(u, holdings(id));
    }

    private List<Holding> holdings(UUID id) {
        return positions.findByUserId(id).stream()
            .filter(p -> p.getShares() > 0)
            .map(p -> new Holding(p.getSymbol(), p.getShares()))
            .toList();
    }

    private UserView view(User u, List<Holding> h) {
        return new UserView(u.getId(), u.getId(), u.getUsername(), u.getCashBalance(),
            u.getCashBalance(), h);
    }
}
