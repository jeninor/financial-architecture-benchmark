package com.juanesteban.tcc.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TradeServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
            TradeServiceApplication.class,
            args
        );
    }

}