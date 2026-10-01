package com.tcc.finance.trade.service;

import com.tcc.finance.trade.client.MarketClient;
import com.tcc.finance.trade.client.UserClient;
import com.tcc.finance.trade.client.dto.BalanceOperationRequest;
import com.tcc.finance.trade.client.dto.QuoteDto;
import com.tcc.finance.trade.client.dto.UserDto;
import com.tcc.finance.trade.domain.Position;
import com.tcc.finance.trade.domain.TradeTransaction;
import com.tcc.finance.trade.domain.TransactionType;
import com.tcc.finance.trade.exception.BadRequestException;
import com.tcc.finance.trade.messaging.TradeCompletedEvent;
import com.tcc.finance.trade.repository.PositionRepository;
import com.tcc.finance.trade.repository.TradeTransactionRepository;
import com.tcc.finance.trade.web.dto.HistoryEntry;
import com.tcc.finance.trade.web.dto.PortfolioResponse;
import com.tcc.finance.trade.web.dto.PositionView;
import com.tcc.finance.trade.web.dto.TradeResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Orquestra compra/venda entre user-service (saldo, com lock de linha), market-service
 * (precos) e a base local (posicoes e transacoes).
 *
 * <p>Limitacao documentada (fora do escopo do experimento): NAO ha transacao distribuida
 * (sem Saga/2PC). O debito/credito no user-service e confirmado no proprio servico; se a
 * gravacao local falhar depois disso, o saldo e a posicao ficam inconsistentes
 * (consistencia eventual nao garantida).
 */
@Service
public class TradeService {

    private final UserClient userClient;
    private final MarketClient marketClient;
    private final PositionRepository positionRepository;
    private final TradeTransactionRepository transactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TradeService(UserClient userClient,
                        MarketClient marketClient,
                        PositionRepository positionRepository,
                        TradeTransactionRepository transactionRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.userClient = userClient;
        this.marketClient = marketClient;
        this.positionRepository = positionRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TradeResponse buy(String username, String rawSymbol, Integer quantity) {
        validateQuantity(quantity);
        userClient.getUser(username);
        QuoteDto quote = marketClient.quote(rawSymbol);
        String symbol = quote.symbol();
        BigDecimal price = quote.price();
        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

        // Lock + verificacao de saldo acontecem atomicamente dentro do user-service.
        UserDto user = userClient.debit(username, new BalanceOperationRequest(total));

        Position position = positionRepository.findByUsernameAndSymbolForUpdate(username, symbol)
                .orElseGet(() -> new Position(username, symbol));
        position.add(quantity);
        positionRepository.save(position);

        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(username, TransactionType.BUY, symbol, quantity, price, Instant.now()));
        return complete(tx, total, user.saldo());
    }

    @Transactional
    public TradeResponse sell(String username, String rawSymbol, Integer quantity) {
        validateQuantity(quantity);
        userClient.getUser(username);
        QuoteDto quote = marketClient.quote(rawSymbol);
        String symbol = quote.symbol();
        BigDecimal price = quote.price();

        Position position = positionRepository.findByUsernameAndSymbolForUpdate(username, symbol).orElse(null);
        int owned = position == null ? 0 : position.getQuantity();
        if (quantity > owned) {
            throw new BadRequestException("Quantidade insuficiente: tentou vender " + quantity
                    + " de " + symbol + ", mas possui " + owned);
        }

        BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
        UserDto user = userClient.credit(username, new BalanceOperationRequest(total));

        position.remove(quantity);
        if (position.getQuantity() == 0) {
            positionRepository.delete(position);
        } else {
            positionRepository.save(position);
        }

        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(username, TransactionType.SELL, symbol, quantity, price, Instant.now()));
        return complete(tx, total, user.saldo());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        UserDto user = userClient.getUser(username);
        List<PositionView> positions = positionRepository.findByUsernameOrderBySymbolAsc(username).stream()
                .map(p -> {
                    BigDecimal price = marketClient.quote(p.getSymbol()).price();
                    return new PositionView(p.getSymbol(), p.getQuantity(), price,
                            price.multiply(BigDecimal.valueOf(p.getQuantity())));
                })
                .toList();
        BigDecimal stocksValue = positions.stream()
                .map(PositionView::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(user.username(), positions, user.saldo(),
                stocksValue, user.saldo().add(stocksValue));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(String username) {
        userClient.getUser(username);
        return transactionRepository.findByUsernameOrderByTimestampAscIdAsc(username).stream()
                .map(t -> new HistoryEntry(t.getId(), t.getType(), t.getSymbol(), t.getQuantity(),
                        t.getPrice(), t.getTimestamp()))
                .toList();
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("quantity deve ser um inteiro positivo (> 0)");
        }
    }

    private TradeResponse complete(TradeTransaction tx, BigDecimal total, BigDecimal saldo) {
        // Publicado no RabbitMQ apenas apos o commit (ver TradeEventPublisher).
        eventPublisher.publishEvent(new TradeCompletedEvent(tx.getId(), tx.getUsername(), tx.getType(),
                tx.getSymbol(), tx.getQuantity(), tx.getPrice(), total, saldo, tx.getTimestamp()));
        return new TradeResponse(tx.getId(), tx.getUsername(), tx.getType(), tx.getSymbol(),
                tx.getQuantity(), tx.getPrice(), total, saldo, tx.getTimestamp());
    }
}
