package com.tcc.finance.trade.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica {@code trade.completed} somente apos o commit local do trade-service,
 * para nao anunciar operacoes que foram revertidas. Falha no broker nao desfaz o trade
 * (o evento e de auditoria); fica registrada em log.
 */
@Component
public class TradeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradeEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public TradeEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTradeCompleted(TradeCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.TRADE_EXCHANGE,
                    RabbitConfig.TRADE_COMPLETED_ROUTING_KEY, event);
        } catch (AmqpException ex) {
            log.error("Falha ao publicar trade.completed (transacao {}): {}", event.transactionId(), ex.getMessage());
        }
    }
}
