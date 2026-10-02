package com.tcc.finance.trade.client;

import com.tcc.finance.trade.exception.InsufficientFundsException;
import com.tcc.finance.trade.exception.UserNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Traduz as respostas de erro HTTP do user-service para as excecoes de
 * dominio do trade-service, usadas pelo {@code GlobalExceptionHandler}
 * para devolver o HTTP status correto ao cliente final do trade-service.
 *
 * <p>Propositalmente NAO anotada com {@code @Configuration}: e referenciada
 * apenas via {@code @FeignClient(configuration = ...)} e carregada no
 * contexto filho especifico do {@link UserServiceClient}. Anota-la geraria
 * um bean global de {@code ErrorDecoder}, afetando (ou conflitando com) os
 * demais clientes Feign.</p>
 */
public class UserServiceClientConfig {

    @Bean
    public ErrorDecoder userServiceErrorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new UserNotFoundException(extractUsername(response));
            }
            if (response.status() == 400) {
                return new InsufficientFundsException();
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }

    private String extractUsername(Response response) {
        // O username ja esta disponivel no contexto de chamada do service;
        // aqui mantemos uma mensagem generica para nao acoplar ao path.
        return response.request() != null ? response.request().url() : "desconhecido";
    }
}
