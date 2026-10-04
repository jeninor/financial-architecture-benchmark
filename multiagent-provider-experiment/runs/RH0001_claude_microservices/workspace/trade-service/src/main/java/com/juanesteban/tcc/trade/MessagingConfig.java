package com.juanesteban.tcc.trade;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String EXCHANGE = "financial.exchange";
    public static final String KEY = "trade.completed";
    public static final String QUEUE = "trade.audit.queue";

    @Bean TopicExchange financialExchange() { return new TopicExchange(EXCHANGE); }
    @Bean Queue tradeAuditQueue() { return new Queue(QUEUE, true); }
    @Bean Binding tradeAuditBinding(Queue tradeAuditQueue, TopicExchange financialExchange) {
        return BindingBuilder.bind(tradeAuditQueue).to(financialExchange).with(KEY);
    }
}
