package com.juanesteban.tcc.user.controller;

import com.juanesteban.tcc.user.dto.TradeRequest;
import com.juanesteban.tcc.user.model.HoldingEntity;
import com.juanesteban.tcc.user.model.UserEntity;
import com.juanesteban.tcc.user.repository.HoldingRepository;
import com.juanesteban.tcc.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

    public InternalUserController(UserRepository userRepository, HoldingRepository holdingRepository) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Void> checkUser(@PathVariable UUID userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/buy")
    @Transactional
    public ResponseEntity<Void> buy(@PathVariable UUID userId, @RequestBody TradeRequest request) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntity user = userOpt.get();

        BigDecimal totalCost = request.getPrice().multiply(BigDecimal.valueOf(request.getShares()));
        if (user.getCashBalance().compareTo(totalCost) < 0) {
            return ResponseEntity.badRequest().build();
        }

        user.setCashBalance(user.getCashBalance().subtract(totalCost));
        userRepository.save(user);

        HoldingEntity holding = holdingRepository.findByUserIdAndSymbol(userId, request.getSymbol())
                .orElseGet(() -> {
                    HoldingEntity h = new HoldingEntity();
                    h.setUserId(userId);
                    h.setSymbol(request.getSymbol());
                    h.setShares(0);
                    return h;
                });

        holding.setShares(holding.getShares() + request.getShares());
        holdingRepository.save(holding);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/sell")
    @Transactional
    public ResponseEntity<Void> sell(@PathVariable UUID userId, @RequestBody TradeRequest request) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserEntity user = userOpt.get();

        Optional<HoldingEntity> holdingOpt = holdingRepository.findByUserIdAndSymbol(userId, request.getSymbol());
        if (holdingOpt.isEmpty() || holdingOpt.get().getShares() < request.getShares()) {
            return ResponseEntity.badRequest().build();
        }

        HoldingEntity holding = holdingOpt.get();
        BigDecimal totalRevenue = request.getPrice().multiply(BigDecimal.valueOf(request.getShares()));

        user.setCashBalance(user.getCashBalance().add(totalRevenue));
        userRepository.save(user);

        holding.setShares(holding.getShares() - request.getShares());
        if (holding.getShares() == 0) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        return ResponseEntity.ok().build();
    }
}
