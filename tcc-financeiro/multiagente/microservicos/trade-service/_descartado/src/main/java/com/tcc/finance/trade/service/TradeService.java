package com.tcc.finance.trade.service;

import com.tcc.finance.trade.client.MarketServiceClient;
import com.tcc.finance.trade.client.UserServiceClient;
import com.tcc.finance.trade.dto.AmountRequest;
import com.tcc.finance.trade.dto.PortfolioResponse;
import com.tcc.finance.trade.dto.PosicaoResponse;
import com.tcc.finance.trade.dto.QuoteDTO;
import com.tcc.finance.trade.dto.TradeResponse;
import com.tcc.finance.trade.dto.TransacaoResponse;
import com.tcc.finance.trade.dto.UserDTO;
import com.tcc.finance.trade.event.TradeAuditPublisher;
import com.tcc.finance.trade.event.TradeCompletedEvent;
import com.tcc.finance.trade.exception.InsufficientFundsException;
import com.tcc.finance.trade.exception.InsufficientSharesException;
import com.tcc.finance.trade.exception.InvalidQuantityException;
import com.tcc.finance.trade.model.Posicao;
import com.tcc.finance.trade.model.Transacao;
import com.tcc.finance.trade.model.TipoTransacao;
import com.tcc.finance.trade.repository.PosicaoRepository;
import com.tcc.finance.trade.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Orquestra compra/venda/portfolio/historico no trade-service, consultando
 * user-service e market-service via OpenFeign.
 *
 * <p><b>Trade-off assumido (fora do escopo do experimento, conforme
 * AGENTE1_ESPECIFICACAO.md):</b> a operacao de trade NAO e uma transacao
 * distribuida. O saldo e debitado/creditado no user-service e a
 * posicao/transacao e persistida localmente no trade-service em passos
 * separados, sem Saga nem 2PC. Se o passo remoto (debito/credito no
 * user-service) tiver sucesso mas a escrita local falhar (ou vice-versa),
 * os dados dos dois servicos podem ficar temporariamente inconsistentes.
 * Isso e tratado como consistencia eventual aceita como limitacao
 * documentada do desenho de microsservicos, nao como um bug a corrigir
 * neste agente.</p>
 */
@Service
public class TradeService {

    private final UserServiceClient userServiceClient;
    private final MarketServiceClient marketServiceClient;
    private final PosicaoRepository posicaoRepository;
    private final TransacaoRepository transacaoRepository;
    private final TradeAuditPublisher tradeAuditPublisher;

    public TradeService(UserServiceClient userServiceClient,
                         MarketServiceClient marketServiceClient,
                         PosicaoRepository posicaoRepository,
                         TransacaoRepository transacaoRepository,
                         TradeAuditPublisher tradeAuditPublisher) {
        this.userServiceClient = userServiceClient;
        this.marketServiceClient = marketServiceClient;
        this.posicaoRepository = posicaoRepository;
        this.transacaoRepository = transacaoRepository;
        this.tradeAuditPublisher = tradeAuditPublisher;
    }

    @Transactional
    public TradeResponse buy(String username, String symbol, long quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }

        UserDTO usuario = userServiceClient.getUser(username);
        QuoteDTO quote = marketServiceClient.getQuote(symbol);
        String symbolUpper = quote.getSymbol() != null ? quote.getSymbol() : symbol.toUpperCase();
        BigDecimal total = quote.getPrice().multiply(BigDecimal.valueOf(quantity));

        if (total.compareTo(usuario.getSaldo()) > 0) {
            throw new InsufficientFundsException();
        }

        UserDTO atualizado = userServiceClient.debit(username, new AmountRequest(total));

        Posicao posicao = posicaoRepository.findByUsernameAndSymbol(username, symbolUpper)
                .orElseGet(() -> new Posicao(username, symbolUpper, 0));
        posicao.setQuantity(posicao.getQuantity() + quantity);
        posicaoRepository.save(posicao);

        Instant agora = Instant.now();
        transacaoRepository.save(new Transacao(username, symbolUpper, quantity, quote.getPrice(), TipoTransacao.COMPRA, agora));

        tradeAuditPublisher.publicar(new TradeCompletedEvent(username, symbolUpper, TipoTransacao.COMPRA.name(), quantity, quote.getPrice(), agora));

        return new TradeResponse(username, symbolUpper, quantity, quote.getPrice(), atualizado.getSaldo());
    }

    @Transactional
    public TradeResponse sell(String username, String symbol, long quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }

        userServiceClient.getUser(username);

        String symbolUpper = symbol.toUpperCase();
        Posicao posicao = posicaoRepository.findByUsernameAndSymbol(username, symbolUpper)
                .orElseThrow(InsufficientSharesException::new);
        if (posicao.getQuantity() < quantity) {
            throw new InsufficientSharesException();
        }

        QuoteDTO quote = marketServiceClient.getQuote(symbol);
        BigDecimal total = quote.getPrice().multiply(BigDecimal.valueOf(quantity));

        UserDTO atualizado = userServiceClient.credit(username, new AmountRequest(total));

        posicao.setQuantity(posicao.getQuantity() - quantity);
        posicaoRepository.save(posicao);

        Instant agora = Instant.now();
        transacaoRepository.save(new Transacao(username, symbolUpper, quantity, quote.getPrice(), TipoTransacao.VENDA, agora));

        tradeAuditPublisher.publicar(new TradeCompletedEvent(username, symbolUpper, TipoTransacao.VENDA.name(), quantity, quote.getPrice(), agora));

        return new TradeResponse(username, symbolUpper, quantity, quote.getPrice(), atualizado.getSaldo());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        UserDTO usuario = userServiceClient.getUser(username);

        List<PosicaoResponse> posicoes = posicaoRepository.findByUsername(username).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> {
                    QuoteDTO quote = marketServiceClient.getQuote(p.getSymbol());
                    BigDecimal valorTotal = quote.getPrice().multiply(BigDecimal.valueOf(p.getQuantity()));
                    return new PosicaoResponse(p.getSymbol(), p.getQuantity(), quote.getPrice(), valorTotal);
                })
                .toList();

        return new PortfolioResponse(username, usuario.getSaldo(), posicoes);
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> historico(String username) {
        userServiceClient.getUser(username);

        return transacaoRepository.findByUsernameOrderByTimestampAsc(username).stream()
                .map(t -> new TransacaoResponse(t.getSymbol(), t.getTipo().name(), t.getQuantity(), t.getPreco(), t.getTimestamp()))
                .toList();
    }
}
