package com.juanesteban.tcc.user.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    public static final String EXCHANGE =
        "financial.exchange";

    public static final String QUEUE =
        "trade.audit.queue";

    public static final String ROUTING_KEY =
        "trade.completed";


    @Bean
    public TopicExchange financialExchange() {

        return new TopicExchange(
            EXCHANGE,
            true,
            false
        );
    }


    @Bean
    public Queue tradeAuditQueue() {

        return new Queue(
            QUEUE,
            true
        );
    }


    @Bean
    public Binding tradeAuditBinding(
        Queue tradeAuditQueue,
        TopicExchange financialExchange
    ) {

        return BindingBuilder
            .bind(
                tradeAuditQueue
            )
            .to(
                financialExchange
            )
            .with(
                ROUTING_KEY
            );
    }
}