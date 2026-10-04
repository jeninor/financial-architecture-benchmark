package com.juanesteban.tcc.trade.controller;

import com.juanesteban.tcc.trade.client.MarketClient;
import com.juanesteban.tcc.trade.client.UserClient;
import com.juanesteban.tcc.trade.config.RabbitMQConfig;
import com.juanesteban.tcc.trade.dto.TradeAuditMessage;
import com.juanesteban.tcc.trade.dto.TradeRequestDto;
import com.juanesteban.tcc.trade.dto.TradeResponseDto;
import com.juanesteban.tcc.trade.model.TradeEntity;
import com.juanesteban.tcc.trade.repository.TradeRepository;
import feign.FeignException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeRepository tradeRepository;
    private final MarketClient marketClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    public TradeController(TradeRepository tradeRepository, MarketClient marketClient, UserClient userClient, RabbitTemplate rabbitTemplate) {
        this.tradeRepository = tradeRepository;
        this.marketClient = marketClient;
        this.userClient = userClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/trades/buy")
    public ResponseEntity<Void> buy(@RequestBody TradeRequestDto request) {
        if (request.getShares() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        MarketClient.QuoteResponse quote;
        try {
            ResponseEntity<MarketClient.QuoteResponse> quoteResponse = marketClient.getQuote(request.getSymbol());
            if (!quoteResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.badRequest().build();
            }
            quote = quoteResponse.getBody();
        } catch (FeignException.BadRequest e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userClient.buy(request.getUserId(), new UserClient.TradeRequest(quote.getSymbol(), request.getShares(), quote.getPrice()));
        } catch (FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.notFound().build();
            } else if (e.status() == 400) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        TradeEntity trade = new TradeEntity();
        trade.setUserId(request.getUserId());
        trade.setSymbol(quote.getSymbol());
        trade.setShares(request.getShares());
        trade.setPrice(quote.getPrice());
        trade.setType("BUY");
        trade.setTimestamp(LocalDateTime.now());
        trade = tradeRepository.save(trade);

        sendAuditMessage(trade);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/trades/sell")
    public ResponseEntity<Void> sell(@RequestBody TradeRequestDto request) {
        if (request.getShares() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        MarketClient.QuoteResponse quote;
        try {
            ResponseEntity<MarketClient.QuoteResponse> quoteResponse = marketClient.getQuote(request.getSymbol());
            if (!quoteResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.badRequest().build();
            }
            quote = quoteResponse.getBody();
        } catch (FeignException.BadRequest e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userClient.sell(request.getUserId(), new UserClient.TradeRequest(quote.getSymbol(), request.getShares(), quote.getPrice()));
        } catch (FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.notFound().build();
            } else if (e.status() == 400) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        TradeEntity trade = new TradeEntity();
        trade.setUserId(request.getUserId());
        trade.setSymbol(quote.getSymbol());
        trade.setShares(request.getShares());
        trade.setPrice(quote.getPrice());
        trade.setType("SELL");
        trade.setTimestamp(LocalDateTime.now());
        trade = tradeRepository.save(trade);

        sendAuditMessage(trade);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/users/{userId}/trades")
    public ResponseEntity<List<TradeResponseDto>> getTrades(@PathVariable UUID userId) {
        try {
            userClient.checkUser(userId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(tradeRepository.findByUserId(userId).stream()
                .map(t -> new TradeResponseDto(t.getId(), t.getUserId(), t.getSymbol(), t.getShares(), t.getPrice(), t.getType(), t.getTimestamp()))
                .collect(Collectors.toList()));
    }

    private void sendAuditMessage(TradeEntity trade) {
        TradeAuditMessage message = new TradeAuditMessage(trade.getId(), trade.getUserId(), trade.getSymbol(), trade.getShares(), trade.getPrice(), trade.getType());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}
