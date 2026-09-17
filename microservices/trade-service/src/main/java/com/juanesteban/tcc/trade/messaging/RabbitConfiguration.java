package com.juanesteban.tcc.trade.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    public static final String EXCHANGE =
        "financial.exchange";

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
}