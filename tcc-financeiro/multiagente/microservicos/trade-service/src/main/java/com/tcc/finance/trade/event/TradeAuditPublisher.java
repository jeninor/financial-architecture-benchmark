package com.tcc.finance.trade.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradeAuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(TradeAuditPublisher.class);
    private static final String QUEUE = "trade.audit.queue";

    private final RabbitTemplate rabbitTemplate;

    public TradeAuditPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica o evento de auditoria apos a compra/venda ja ter sido
     * confirmada (saldo debitado/creditado, posicao e transacao
     * persistidas). Uma falha na publicacao NAO desfaz a operacao: e um
     * trade-off de consistencia eventual aceito explicitamente pela
     * especificacao do TCC (sem Saga/2PC), e por isso o erro e apenas
     * registrado em log.
     */
    public void publicar(TradeCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(QUEUE, event);
        } catch (Exception ex) {
            log.warn("Falha ao publicar evento trade.completed para auditoria (username={}, symbol={}): {}. "
                            + "Operacao de trade já confirmada; consistencia eventual aceita como trade-off.",
                    event.getUsername(), event.getSymbol(), ex.getMessage());
        }
    }
}
