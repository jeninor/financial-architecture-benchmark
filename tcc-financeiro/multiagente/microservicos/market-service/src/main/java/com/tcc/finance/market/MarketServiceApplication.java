package com.tcc.finance.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class MarketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceApplication.class, args);
    }

    @RestController
    static class HealthController {
        @GetMapping("/health")
        public String health() {
            return "market-service-ok";
        }
    }
}
