package com.tcc.finance.trade.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.finance.trade.exception.BadRequestException;
import com.tcc.finance.trade.exception.ConflictException;
import com.tcc.finance.trade.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Traduz as respostas de erro do user-service/market-service para as excecoes de
 * dominio do trade-service, preservando o status HTTP (404/400/409) e a mensagem.
 */
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder fallback = new ErrorDecoder.Default();
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BadRequestException(message(response, "Requisicao invalida"));
            case 404 -> new NotFoundException(message(response, "Recurso nao encontrado"));
            case 409 -> new ConflictException(message(response, "Conflito"));
            default -> fallback.decode(methodKey, response);
        };
    }

    private String message(Response response, String defaultMessage) {
        if (response.body() == null) {
            return defaultMessage;
        }
        try (InputStream body = response.body().asInputStream()) {
            JsonNode node = objectMapper.readTree(body);
            JsonNode message = node == null ? null : node.get("message");
            return message == null || message.isNull() ? defaultMessage : message.asText();
        } catch (IOException ex) {
            return defaultMessage;
        }
    }
}
