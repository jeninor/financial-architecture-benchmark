package com.tcc.finance.trade.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String TRADE_EXCHANGE = "trade.events";
    public static final String TRADE_COMPLETED_ROUTING_KEY = "trade.completed";

    @Bean
    public TopicExchange tradeExchange() {
        return new TopicExchange(TRADE_EXCHANGE, true, false);
    }

    @Bean
    public Binding tradeAuditBinding(Queue tradeAuditQueue, TopicExchange tradeExchange) {
        return BindingBuilder.bind(tradeAuditQueue).to(tradeExchange).with(TRADE_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
