package com.tcc.finance.trade.client;

import com.tcc.finance.trade.dto.QuoteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente OpenFeign para o market-service (cotacoes). Descoberto via Eureka
 * pelo "name" (registrado como "market-service"). Substituido por
 * @MockBean nos testes do trade-service.
 */
@FeignClient(name = "market-service", configuration = MarketServiceClientConfig.class)
public interface MarketServiceClient {

    @GetMapping("/quote/{symbol}")
    QuoteDTO getQuote(@PathVariable("symbol") String symbol);
}
