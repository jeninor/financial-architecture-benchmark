package com.tcc.finance.trade.client;

import com.tcc.finance.trade.dto.AmountRequest;
import com.tcc.finance.trade.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente OpenFeign para o user-service (saldo/debito/credito). Descoberto
 * via Eureka pelo "name" (registrado como "user-service" no application.properties).
 * Em producao, erros HTTP sao traduzidos pelo {@link UserServiceClientConfig}
 * para as excecoes de dominio do trade-service; nos testes, este cliente e
 * substituido por um @MockBean para nao depender do user-service real.
 */
@FeignClient(name = "user-service", configuration = UserServiceClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/users/{username}")
    UserDTO getUser(@PathVariable("username") String username);

    @PostMapping("/users/{username}/debit")
    UserDTO debit(@PathVariable("username") String username, @RequestBody AmountRequest request);

    @PostMapping("/users/{username}/credit")
    UserDTO credit(@PathVariable("username") String username, @RequestBody AmountRequest request);
}
