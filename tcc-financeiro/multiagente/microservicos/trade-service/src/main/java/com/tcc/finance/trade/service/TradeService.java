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
 * Compra/venda/portfolio/historico. Saldo vive no user-service e cotacoes no
 * market-service (OpenFeign). Sem Saga/2PC: o debito/credito remoto acontece antes
 * da escrita local, e o evento trade.completed so e publicado apos o commit local
 * (ver TradeEventPublisher).
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
    public TradeResponse buy(String username, String symbol, Integer quantity) {
        int qty = requirePositive(quantity);
        userClient.getUser(username);
        QuoteDto quote = marketClient.quote(symbol);
        BigDecimal total = quote.price().multiply(BigDecimal.valueOf(qty));

        // O user-service valida o saldo (400 se insuficiente) sob lock pessimista.
        UserDto updated = userClient.debit(username, new BalanceOperationRequest(total));

        Position position = positionRepository.findByUsernameAndSymbolForUpdate(username, quote.symbol())
                .orElseGet(() -> new Position(username, quote.symbol()));
        position.add(qty);
        positionRepository.save(position);

        return record(updated, TransactionType.BUY, quote, qty, total);
    }

    @Transactional
    public TradeResponse sell(String username, String symbol, Integer quantity) {
        int qty = requirePositive(quantity);
        userClient.getUser(username);
        QuoteDto quote = marketClient.quote(symbol);

        Position position = positionRepository.findByUsernameAndSymbolForUpdate(username, quote.symbol())
                .orElseThrow(() -> new BadRequestException("Usuario nao possui acoes de " + quote.symbol()));
        if (position.getQuantity() < qty) {
            throw new BadRequestException("Quantidade insuficiente: possui " + position.getQuantity()
                    + ", tentou vender " + qty);
        }

        BigDecimal total = quote.price().multiply(BigDecimal.valueOf(qty));
        UserDto updated = userClient.credit(username, new BalanceOperationRequest(total));
        position.remove(qty);
        positionRepository.save(position);

        return record(updated, TransactionType.SELL, quote, qty, total);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        UserDto user = userClient.getUser(username);
        List<PositionView> positions = positionRepository.findByUsernameOrderBySymbolAsc(username).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> {
                    BigDecimal currentPrice = marketClient.quote(p.getSymbol()).price();
                    return new PositionView(p.getSymbol(), p.getQuantity(), currentPrice,
                            currentPrice.multiply(BigDecimal.valueOf(p.getQuantity())));
                })
                .toList();
        BigDecimal stocksValue = positions.stream()
                .map(PositionView::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(user.username(), positions, user.saldo(), stocksValue,
                user.saldo().add(stocksValue));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(String username) {
        userClient.getUser(username);
        return transactionRepository.findByUsernameOrderByTimestampAscIdAsc(username).stream()
                .map(t -> new HistoryEntry(t.getId(), t.getType(), t.getSymbol(), t.getQuantity(),
                        t.getPrice(), t.getTimestamp()))
                .toList();
    }

    private TradeResponse record(UserDto user, TransactionType type, QuoteDto quote, int qty, BigDecimal total) {
        TradeTransaction tx = transactionRepository.save(
                new TradeTransaction(user.username(), type, quote.symbol(), qty, quote.price(), Instant.now()));
        eventPublisher.publishEvent(new TradeCompletedEvent(tx.getId(), user.username(), type, quote.symbol(),
                qty, quote.price(), total, user.saldo(), tx.getTimestamp()));
        return new TradeResponse(tx.getId(), user.username(), type, quote.symbol(), qty, quote.price(), total,
                user.saldo(), tx.getTimestamp());
    }

    private static int requirePositive(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("quantity deve ser um inteiro positivo");
        }
        return quantity;
    }
}
