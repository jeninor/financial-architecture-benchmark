package com.tcc.finance.service;

import com.tcc.finance.domain.Position;
import com.tcc.finance.domain.TradeTransaction;
import com.tcc.finance.domain.TransactionType;
import com.tcc.finance.domain.UserAccount;
import com.tcc.finance.exception.BadRequestException;
import com.tcc.finance.exception.NotFoundException;
import com.tcc.finance.repository.PositionRepository;
import com.tcc.finance.repository.TradeTransactionRepository;
import com.tcc.finance.repository.UserAccountRepository;
import com.tcc.finance.web.dto.HistoryEntry;
import com.tcc.finance.web.dto.PortfolioResponse;
import com.tcc.finance.web.dto.PositionView;
import com.tcc.finance.web.dto.TradeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class TradeService {

    private final UserAccountRepository userRepository;
    private final PositionRepository positionRepository;
    private final TradeTransactionRepository transactionRepository;
    private final QuoteService quoteService;

    public TradeService(UserAccountRepository userRepository,
                        PositionRepository positionRepository,
                        TradeTransactionRepository transactionRepository,
                        QuoteService quoteService) {
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.transactionRepository = transactionRepository;
        this.quoteService = quoteService;
    }

    @Transactional
    public TradeResponse buy(String username, String rawSymbol, Integer quantity) {
        validateQuantity(quantity);
        UserAccount user = lockUser(username);
        String symbol = quoteService.normalize(rawSymbol);
        BigDecimal price = quoteService.getPrice(symbol);
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

        if (total.compareTo(user.getSaldo()) > 0) {
            throw new BadRequestException("Saldo insuficiente: custo " + total
                    + " excede o saldo disponivel " + user.getSaldo());
        }

        user.debit(total);
        Position position = positionRepository.findByUserAndSymbol(user, symbol)
                .orElseGet(() -> new Position(user, symbol));
        position.add(quantity);
        positionRepository.save(position);

        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(user, TransactionType.BUY, symbol, quantity, price, Instant.now()));
        return toResponse(tx, total, user.getSaldo());
    }

    @Transactional
    public TradeResponse sell(String username, String rawSymbol, Integer quantity) {
        validateQuantity(quantity);
        UserAccount user = lockUser(username);
        String symbol = quoteService.normalize(rawSymbol);
        BigDecimal price = quoteService.getPrice(symbol);

        Position position = positionRepository.findByUserAndSymbol(user, symbol).orElse(null);
        int owned = position == null ? 0 : position.getQuantity();
        if (quantity > owned) {
            throw new BadRequestException("Quantidade insuficiente: tentou vender " + quantity
                    + " de " + symbol + ", mas possui " + owned);
        }

        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        user.credit(total);
        position.remove(quantity);
        if (position.getQuantity() == 0) {
            positionRepository.delete(position);
        } else {
            positionRepository.save(position);
        }

        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(user, TransactionType.SELL, symbol, quantity, price, Instant.now()));
        return toResponse(tx, total, user.getSaldo());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        UserAccount user = findUser(username);
        List<PositionView> positions = positionRepository.findByUserOrderBySymbolAsc(user).stream()
                .map(p -> {
                    BigDecimal price = quoteService.getPrice(p.getSymbol());
                    return new PositionView(p.getSymbol(), p.getQuantity(), price,
                            price.multiply(BigDecimal.valueOf(p.getQuantity())));
                })
                .toList();
        BigDecimal stocksValue = positions.stream()
                .map(PositionView::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(user.getUsername(), positions, user.getSaldo(),
                stocksValue, user.getSaldo().add(stocksValue));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(String username) {
        UserAccount user = findUser(username);
        return transactionRepository.findByUserOrderByTimestampAscIdAsc(user).stream()
                .map(t -> new HistoryEntry(t.getId(), t.getType(), t.getSymbol(), t.getQuantity(),
                        t.getPrice(), t.getTimestamp()))
                .toList();
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("quantity deve ser um inteiro positivo (> 0)");
        }
    }

    private UserAccount findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }

    private UserAccount lockUser(String username) {
        return userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }

    private TradeResponse toResponse(TradeTransaction tx, BigDecimal total, BigDecimal saldo) {
        return new TradeResponse(tx.getId(), tx.getUser().getUsername(), tx.getType(), tx.getSymbol(),
                tx.getQuantity(), tx.getPrice(), total, saldo, tx.getTimestamp());
    }
}
