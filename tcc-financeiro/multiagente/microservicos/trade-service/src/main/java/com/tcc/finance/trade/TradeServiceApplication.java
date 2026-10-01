package com.tcc.finance.trade;

import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TradeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeServiceApplication.class, args);
    }

    @Bean
    public Queue tradeAuditQueue() {
        return new Queue("trade.audit.queue", true);
    }

    @RestController
    static class HealthController {
        @GetMapping("/health")
        public String health() {
            return "trade-service-ok";
        }
    }
}
