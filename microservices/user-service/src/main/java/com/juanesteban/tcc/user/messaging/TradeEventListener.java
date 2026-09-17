package com.juanesteban.tcc.user.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TradeEventListener {

    private static final Logger log =
        LoggerFactory.getLogger(
            TradeEventListener.class
        );


    @RabbitListener(
        queues = RabbitConfiguration.QUEUE
    )
    public void receive(
        String event
    ) {

        log.info(
            "AUDIT trade.completed: {}",
            event
        );
    }
}