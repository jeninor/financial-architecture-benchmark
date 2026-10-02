package com.tcc.finance.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre, no user-service, a parte dos cenarios T01-T12 que pertence a este
 * modulo: T03 (criacao com saldo inicial), T04 (username duplicado) e T12
 * (usuario inexistente), alem dos endpoints internos de debito/credito
 * usados pelo trade-service via OpenFeign.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void T03_criacaoUsuarioComSaldoInicial() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "t03user"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("t03user"))
                .andExpect(jsonPath("$.saldo").value(closeTo(10000.00, 0.001)));
    }

    @Test
    void T04_usernameDuplicado() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "t04user"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "t04user"))))
                .andExpect(status().isConflict());
    }

    @Test
    void T12_usuarioInexistente() throws Exception {
        mockMvc.perform(get("/users/naoexiste"))
                .andExpect(status().isNotFound());
    }

    @Test
    void debitoComSaldoSuficiente() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "debitouser"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users/debitouser/debit")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("amount", 1500.00))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(8500.00, 0.001)));
    }

    @Test
    void debitoComSaldoInsuficiente() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "debitouser2"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users/debitouser2/debit")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("amount", 999999.00))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creditoAtualizaSaldo() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "creditouser"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/users/creditouser/credit")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("amount", 500.00))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(10500.00, 0.001)));
    }
}
