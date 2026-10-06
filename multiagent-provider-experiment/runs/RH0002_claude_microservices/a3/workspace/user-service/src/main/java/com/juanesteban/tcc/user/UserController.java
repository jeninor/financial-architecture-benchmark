package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserRepository users;
    private final PositionRepository positions;

    public UserController(UserRepository users, PositionRepository positions) {
        this.users = users;
        this.positions = positions;
    }

    public record CreateUser(String username) {}
    public record Apply(String type, String symbol, Integer shares, BigDecimal price) {}

    private static UUID parse(String s) {
        try {
            return UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> view(User u) {
        List<Map<String, Object>> holdings = new ArrayList<>();
        for (Position p : positions.findByUserId(u.getId())) {
            if (p.getShares() > 0) {
                holdings.add(Map.of("symbol", p.getSymbol(), "shares", p.getShares()));
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("userId", u.getId());
        m.put("username", u.getUsername());
        m.put("cashBalance", u.getCashBalance());
        m.put("cash", u.getCashBalance());
        m.put("holdings", holdings);
        return m;
    }

    @PostMapping("/api/users")
    public ResponseEntity<?> create(@RequestBody CreateUser req) {
        if (req == null || req.username() == null || req.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username required"));
        }
        String name = req.username().trim();
        if (users.existsByUsername(name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }
        try {
            User u = users.saveAndFlush(new User(name));
            return ResponseEntity.status(HttpStatus.CREATED).body(view(u));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }
    }

    @GetMapping("/internal/users/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        UUID uid = parse(id);
        Optional<User> u = uid == null ? Optional.empty() : users.findById(uid);
        if (u.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
        }
        return ResponseEntity.ok(view(u.get()));
    }

    /** Atomically validates and applies a trade to cash and position. */
    @PostMapping("/internal/users/{id}/apply")
    @Transactional
    public ResponseEntity<?> apply(@PathVariable String id, @RequestBody Apply req) {
        UUID uid = parse(id);
        Optional<User> opt = uid == null ? Optional.empty() : users.findForUpdate(uid);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
        }
        User u = opt.get();
        if (req.shares() == null || req.shares() <= 0 || req.price() == null || req.symbol() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid request"));
        }
        BigDecimal total = req.price().multiply(BigDecimal.valueOf(req.shares()));
        Optional<Position> pos = positions.findByUserIdAndSymbol(uid, req.symbol());
        String error;
        if ("BUY".equals(req.type())) {
            error = applyBuy(u, pos, req, total);
        } else if ("SELL".equals(req.type())) {
            error = applySell(u, pos, req, total);
        } else {
            error = "invalid type";
        }
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(view(u));
    }

    /** Returns an error message, or null on success. */
    private String applyBuy(User u, Optional<Position> pos, Apply req, BigDecimal total) {
        if (u.getCashBalance().compareTo(total) < 0) {
            return "insufficient funds";
        }
        u.setCashBalance(u.getCashBalance().subtract(total));
        if (pos.isPresent()) {
            pos.get().setShares(pos.get().getShares() + req.shares());
        } else {
            positions.save(new Position(u.getId(), req.symbol(), req.shares()));
        }
        return null;
    }

    /** Returns an error message, or null on success. */
    private String applySell(User u, Optional<Position> pos, Apply req, BigDecimal total) {
        if (pos.isEmpty() || pos.get().getShares() < req.shares()) {
            return "insufficient position";
        }
        pos.get().setShares(pos.get().getShares() - req.shares());
        u.setCashBalance(u.getCashBalance().add(total));
        return null;
    }
}
