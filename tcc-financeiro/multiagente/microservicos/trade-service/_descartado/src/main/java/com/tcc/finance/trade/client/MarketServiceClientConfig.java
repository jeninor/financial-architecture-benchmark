package com.tcc.finance.trade.client;

import com.tcc.finance.trade.exception.SymbolNotFoundException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Traduz as respostas de erro HTTP do market-service (404 = simbolo
 * invalido) para a excecao de dominio do trade-service.
 *
 * <p>Propositalmente NAO anotada com {@code @Configuration} (ver
 * {@link UserServiceClientConfig} para o motivo).</p>
 */
public class MarketServiceClientConfig {

    @Bean
    public ErrorDecoder marketServiceErrorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new SymbolNotFoundException(response.request() != null ? response.request().url() : "desconhecido");
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
