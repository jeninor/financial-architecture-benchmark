package com.tcc.finance.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.finance.trade.client.FeignErrorDecoder;
import com.tcc.finance.trade.exception.BadRequestException;
import com.tcc.finance.trade.exception.NotFoundException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FeignErrorDecoderTest {

    private final FeignErrorDecoder decoder = new FeignErrorDecoder(new ObjectMapper());

    private Response response(int status, String body) {
        Request request = Request.create(Request.HttpMethod.POST, "http://user-service/users/x/debit",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return Response.builder().status(status).request(request)
                .body(body, StandardCharsets.UTF_8).build();
    }

    @Test
    void traduz404ParaNotFound() {
        Exception ex = decoder.decode("UserClient#getUser", response(404, "{\"message\":\"Usuario nao encontrado: x\"}"));
        assertInstanceOf(NotFoundException.class, ex);
        assertEquals("Usuario nao encontrado: x", ex.getMessage());
    }

    @Test
    void traduz400ParaBadRequest() {
        Exception ex = decoder.decode("UserClient#debit", response(400, "{\"message\":\"Saldo insuficiente\"}"));
        assertInstanceOf(BadRequestException.class, ex);
        assertEquals("Saldo insuficiente", ex.getMessage());
    }
}
