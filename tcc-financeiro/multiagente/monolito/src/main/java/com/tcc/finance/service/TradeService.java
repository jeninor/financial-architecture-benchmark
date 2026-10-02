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
    public TradeResponse buy(String username, String symbol, Integer quantity) {
        int qty = requirePositive(quantity);
        UserAccount user = lockUser(username);
        String normalized = quoteService.normalize(symbol);
        BigDecimal price = quoteService.getPrice(normalized);
        BigDecimal total = price.multiply(BigDecimal.valueOf(qty));

        if (total.compareTo(user.getSaldo()) > 0) {
            throw new BadRequestException("Saldo insuficiente: necessario " + total + ", disponivel " + user.getSaldo());
        }

        user.debit(total);
        Position position = positionRepository.findByUserAndSymbol(user, normalized)
                .orElseGet(() -> new Position(user, normalized));
        position.add(qty);
        positionRepository.save(position);

        return record(user, TransactionType.BUY, normalized, qty, price, total);
    }

    @Transactional
    public TradeResponse sell(String username, String symbol, Integer quantity) {
        int qty = requirePositive(quantity);
        UserAccount user = lockUser(username);
        String normalized = quoteService.normalize(symbol);
        BigDecimal price = quoteService.getPrice(normalized);

        Position position = positionRepository.findByUserAndSymbol(user, normalized)
                .orElseThrow(() -> new BadRequestException("Usuario nao possui acoes de " + normalized));
        if (position.getQuantity() < qty) {
            throw new BadRequestException("Quantidade insuficiente: possui " + position.getQuantity()
                    + ", tentou vender " + qty);
        }

        BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
        user.credit(total);
        position.remove(qty);
        positionRepository.save(position);

        return record(user, TransactionType.SELL, normalized, qty, price, total);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        UserAccount user = findUser(username);
        List<PositionView> positions = positionRepository.findByUserOrderBySymbolAsc(user).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> {
                    BigDecimal currentPrice = quoteService.getPrice(p.getSymbol());
                    return new PositionView(p.getSymbol(), p.getQuantity(), currentPrice,
                            currentPrice.multiply(BigDecimal.valueOf(p.getQuantity())));
                })
                .toList();
        BigDecimal stocksValue = positions.stream()
                .map(PositionView::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(user.getUsername(), positions, user.getSaldo(), stocksValue,
                user.getSaldo().add(stocksValue));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(String username) {
        UserAccount user = findUser(username);
        return transactionRepository.findByUserOrderByTimestampAscIdAsc(user).stream()
                .map(t -> new HistoryEntry(t.getId(), t.getType(), t.getSymbol(), t.getQuantity(),
                        t.getPrice(), t.getTimestamp()))
                .toList();
    }

    private TradeResponse record(UserAccount user, TransactionType type, String symbol, int qty,
                                 BigDecimal price, BigDecimal total) {
        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(user, type, symbol, qty, price, Instant.now()));
        return new TradeResponse(tx.getId(), user.getUsername(), type, symbol, qty, price, total,
                user.getSaldo(), tx.getTimestamp());
    }

    private static int requirePositive(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("quantity deve ser um inteiro positivo");
        }
        return quantity;
    }

    private UserAccount lockUser(String username) {
        return userRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }

    private UserAccount findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + username));
    }
}
