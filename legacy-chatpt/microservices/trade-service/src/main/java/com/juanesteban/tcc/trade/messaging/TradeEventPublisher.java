package com.juanesteban.tcc.trade.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TradeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;


    public TradeEventPublisher(
        RabbitTemplate rabbitTemplate,
        ObjectMapper objectMapper
    ) {

        this.rabbitTemplate =
            rabbitTemplate;

        this.objectMapper =
            objectMapper;
    }


    public void publish(
        TradeCompletedEvent event
    ) {

        try {

            String json =
                objectMapper
                    .writeValueAsString(
                        event
                    );


            rabbitTemplate.convertAndSend(

                RabbitConfiguration.EXCHANGE,

                RabbitConfiguration.ROUTING_KEY,

                json
            );

        }
        catch (
            JsonProcessingException ex
        ) {

            throw new IllegalStateException(
                "Unable to serialize trade event",
                ex
            );
        }
    }
}