package com.tcc.finance.service;

import com.tcc.finance.dto.PortfolioResponse;
import com.tcc.finance.dto.PosicaoResponse;
import com.tcc.finance.dto.TradeResponse;
import com.tcc.finance.dto.TransacaoResponse;
import com.tcc.finance.exception.InsufficientFundsException;
import com.tcc.finance.exception.InsufficientSharesException;
import com.tcc.finance.exception.InvalidQuantityException;
import com.tcc.finance.model.Posicao;
import com.tcc.finance.model.Transacao;
import com.tcc.finance.model.TipoTransacao;
import com.tcc.finance.model.Usuario;
import com.tcc.finance.repository.PosicaoRepository;
import com.tcc.finance.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class TradeService {

    private final UsuarioService usuarioService;
    private final MarketDataService marketDataService;
    private final PosicaoRepository posicaoRepository;
    private final TransacaoRepository transacaoRepository;

    public TradeService(UsuarioService usuarioService,
                         MarketDataService marketDataService,
                         PosicaoRepository posicaoRepository,
                         TransacaoRepository transacaoRepository) {
        this.usuarioService = usuarioService;
        this.marketDataService = marketDataService;
        this.posicaoRepository = posicaoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public TradeResponse buy(String username, String symbol, long quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }
        Usuario usuario = usuarioService.buscar(username);
        BigDecimal preco = marketDataService.getPrice(symbol);
        BigDecimal total = preco.multiply(BigDecimal.valueOf(quantity));

        if (total.compareTo(usuario.getSaldo()) > 0) {
            throw new InsufficientFundsException();
        }

        usuario.setSaldo(usuario.getSaldo().subtract(total));

        String symbolUpper = symbol.toUpperCase();
        Posicao posicao = posicaoRepository.findByUsuarioAndSymbol(usuario, symbolUpper)
                .orElseGet(() -> new Posicao(usuario, symbolUpper, 0));
        posicao.setQuantity(posicao.getQuantity() + quantity);
        posicaoRepository.save(posicao);

        Transacao transacao = new Transacao(usuario, symbolUpper, quantity, preco, TipoTransacao.COMPRA, Instant.now());
        transacaoRepository.save(transacao);

        return new TradeResponse(usuario.getUsername(), symbolUpper, quantity, preco, usuario.getSaldo());
    }

    @Transactional
    public TradeResponse sell(String username, String symbol, long quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException(quantity);
        }
        Usuario usuario = usuarioService.buscar(username);
        BigDecimal preco = marketDataService.getPrice(symbol);
        String symbolUpper = symbol.toUpperCase();

        Posicao posicao = posicaoRepository.findByUsuarioAndSymbol(usuario, symbolUpper)
                .orElseThrow(InsufficientSharesException::new);
        if (posicao.getQuantity() < quantity) {
            throw new InsufficientSharesException();
        }

        BigDecimal total = preco.multiply(BigDecimal.valueOf(quantity));
        usuario.setSaldo(usuario.getSaldo().add(total));
        posicao.setQuantity(posicao.getQuantity() - quantity);
        posicaoRepository.save(posicao);

        Transacao transacao = new Transacao(usuario, symbolUpper, quantity, preco, TipoTransacao.VENDA, Instant.now());
        transacaoRepository.save(transacao);

        return new TradeResponse(usuario.getUsername(), symbolUpper, quantity, preco, usuario.getSaldo());
    }

    @Transactional(readOnly = true)
    public PortfolioResponse portfolio(String username) {
        Usuario usuario = usuarioService.buscar(username);
        List<PosicaoResponse> posicoes = posicaoRepository.findByUsuario(usuario).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(p -> {
                    BigDecimal precoAtual = marketDataService.getPrice(p.getSymbol());
                    BigDecimal valorTotal = precoAtual.multiply(BigDecimal.valueOf(p.getQuantity()));
                    return new PosicaoResponse(p.getSymbol(), p.getQuantity(), precoAtual, valorTotal);
                })
                .toList();
        return new PortfolioResponse(usuario.getUsername(), usuario.getSaldo(), posicoes);
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> historico(String username) {
        Usuario usuario = usuarioService.buscar(username);
        return transacaoRepository.findByUsuarioOrderByTimestampAsc(usuario).stream()
                .map(t -> new TransacaoResponse(t.getSymbol(), t.getTipo().name(), t.getQuantity(), t.getPreco(), t.getTimestamp()))
                .toList();
    }
}
