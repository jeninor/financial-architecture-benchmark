package com.juanesteban.tcc.trade;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MessagingConfig {
    @Bean
    DirectExchange financialExchange() {
        return new DirectExchange("financial.exchange", true, false);
    }

    @Bean
    Queue auditQueue() {
        return new Queue("trade.audit.queue", true);
    }

    @Bean
    Binding auditBinding(Queue auditQueue, DirectExchange financialExchange) {
        return BindingBuilder.bind(auditQueue).to(financialExchange).with("trade.completed");
    }
}
