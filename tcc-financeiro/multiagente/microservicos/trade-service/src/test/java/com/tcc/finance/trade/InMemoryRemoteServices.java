package com.tcc.finance.trade;

import com.tcc.finance.trade.client.MarketClient;
import com.tcc.finance.trade.client.UserClient;
import com.tcc.finance.trade.client.dto.BalanceOperationRequest;
import com.tcc.finance.trade.client.dto.QuoteDto;
import com.tcc.finance.trade.client.dto.UserDto;
import com.tcc.finance.trade.exception.BadRequestException;
import com.tcc.finance.trade.exception.NotFoundException;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementacoes em memoria com o mesmo contrato do user-service e do market-service
 * (as excecoes sao as que o FeignErrorDecoder produz a partir das respostas HTTP).
 */
final class InMemoryRemoteServices {

    private InMemoryRemoteServices() {
    }

    static MarketClient marketClient() {
        Map<String, BigDecimal> prices = Map.of(
                "AAPL", new BigDecimal("150.00"),
                "GOOG", new BigDecimal("2800.00"),
                "MSFT", new BigDecimal("300.00"),
                "AMZN", new BigDecimal("3300.00"));
        return symbol -> {
            String normalized = symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
            BigDecimal price = prices.get(normalized);
            if (price == null) {
                throw new NotFoundException("Simbolo nao encontrado: " + symbol);
            }
            return new QuoteDto(normalized, price);
        };
    }

    static class FakeUserClient implements UserClient {

        private final Map<String, UserDto> users = new ConcurrentHashMap<>();
        private final AtomicLong ids = new AtomicLong();

        void create(String username) {
            users.put(username, new UserDto(ids.incrementAndGet(), username, new BigDecimal("10000.00")));
        }

        @Override
        public UserDto getUser(String username) {
            UserDto user = users.get(username);
            if (user == null) {
                throw new NotFoundException("Usuario nao encontrado: " + username);
            }
            return user;
        }

        @Override
        public synchronized UserDto debit(String username, BalanceOperationRequest request) {
            UserDto user = getUser(username);
            if (request.amount().compareTo(user.saldo()) > 0) {
                throw new BadRequestException("Saldo insuficiente");
            }
            return update(user, user.saldo().subtract(request.amount()));
        }

        @Override
        public synchronized UserDto credit(String username, BalanceOperationRequest request) {
            UserDto user = getUser(username);
            return update(user, user.saldo().add(request.amount()));
        }

        private UserDto update(UserDto user, BigDecimal saldo) {
            UserDto updated = new UserDto(user.id(), user.username(), saldo);
            users.put(user.username(), updated);
            return updated;
        }
    }
}
