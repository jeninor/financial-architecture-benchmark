package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository users;
    private final PositionRepository positions;
    private final RestClient market;

    public UserService(UserRepository users, PositionRepository positions, RestClient.Builder lbBuilder) {
        this.users = users;
        this.positions = positions;
        this.market = lbBuilder.baseUrl("http://market-service").build();
    }

    public UserAccount create(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username required");
        }
        String name = username.trim();
        if (users.existsByUsername(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        try {
            return users.saveAndFlush(new UserAccount(name));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
    }

    public UserAccount get(UUID id) {
        return users.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    @Transactional
    public Map<String, Object> apply(UUID id, String type, String symbol, long shares, BigDecimal amount) {
        UserAccount u = users.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        Position p = positions.findByUserIdAndSymbol(id, symbol).orElse(null);
        if ("BUY".equals(type)) {
            if (u.getCash().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient funds");
            }
            u.setCash(u.getCash().subtract(amount));
            if (p == null) {
                positions.save(new Position(id, symbol, shares));
            } else {
                p.setShares(p.getShares() + shares);
            }
        } else {
            if (p == null || p.getShares() < shares) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "insufficient position");
            }
            u.setCash(u.getCash().add(amount));
            p.setShares(p.getShares() - shares);
        }
        return Map.of("userId", id, "cash", u.getCash());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> portfolio(UUID id) {
        UserAccount u = get(id);
        List<Map<String, Object>> holdings = new ArrayList<>();
        BigDecimal total = u.getCash();
        for (Position p : positions.findByUserId(id)) {
            if (p.getShares() <= 0) {
                continue;
            }
            Map<String, Object> quote = market.get().uri("/api/quotes/{s}", p.getSymbol())
                .retrieve().body(Map.class);
            BigDecimal price = new BigDecimal(quote.get("price").toString());
            BigDecimal value = price.multiply(BigDecimal.valueOf(p.getShares()));
            total = total.add(value);
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("symbol", p.getSymbol());
            h.put("shares", p.getShares());
            h.put("price", price);
            h.put("marketValue", value);
            holdings.add(h);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", id);
        out.put("username", u.getUsername());
        out.put("cash", u.getCash());
        out.put("cashBalance", u.getCash());
        out.put("holdings", holdings);
        out.put("totalValue", total);
        return out;
    }
}
