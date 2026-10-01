package com.tcc.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre os cenarios T01-T12 definidos em infra/scripts/AGENTE1_ESPECIFICACAO.md.
 * Usa um datasource H2 em memoria (perfil "test") para nao depender de um
 * Postgres real durante `mvn test`.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinanceMonolitoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void criarUsuario(String username) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username))))
                .andExpect(status().isCreated());
    }

    @Test
    void T01_cotacaoValida() throws Exception {
        mockMvc.perform(get("/quote/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.price").value(closeTo(150.00, 0.001)));
    }

    @Test
    void T02_simboloInvalido() throws Exception {
        mockMvc.perform(get("/quote/NAOEXISTE"))
                .andExpect(status().isNotFound());
    }

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
        criarUsuario("t04user");
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", "t04user"))))
                .andExpect(status().isConflict());
    }

    @Test
    void T05_compraValida() throws Exception {
        criarUsuario("t05user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t05user", "symbol", "AAPL", "quantity", 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(8500.00, 0.001)));
    }

    @Test
    void T06_compraSemSaldoSuficiente() throws Exception {
        criarUsuario("t06user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t06user", "symbol", "AMZN", "quantity", 100))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T07_quantidadeIgualAZero() throws Exception {
        criarUsuario("t07user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t07user", "symbol", "AAPL", "quantity", 0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T08_calculoDoPortfolio() throws Exception {
        criarUsuario("t08user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t08user", "symbol", "MSFT", "quantity", 5))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/portfolio/t08user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicoes", hasSize(1)))
                .andExpect(jsonPath("$.posicoes[0].symbol").value("MSFT"))
                .andExpect(jsonPath("$.posicoes[0].quantity").value(5))
                .andExpect(jsonPath("$.posicoes[0].valorTotal").value(closeTo(1500.00, 0.001)))
                .andExpect(jsonPath("$.saldo").value(closeTo(8500.00, 0.001)));
    }

    @Test
    void T09_vendaValida() throws Exception {
        criarUsuario("t09user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t09user", "symbol", "AAPL", "quantity", 10))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sell")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t09user", "symbol", "AAPL", "quantity", 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(9100.00, 0.001)));
    }

    @Test
    void T10_vendaAcimaDaQuantidadeDisponivel() throws Exception {
        criarUsuario("t10user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t10user", "symbol", "AAPL", "quantity", 2))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sell")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t10user", "symbol", "AAPL", "quantity", 99))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T11_historicoDeOperacoes() throws Exception {
        criarUsuario("t11user");
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t11user", "symbol", "AAPL", "quantity", 10))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/sell")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "t11user", "symbol", "AAPL", "quantity", 3))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/history/t11user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("COMPRA"))
                .andExpect(jsonPath("$[1].tipo").value("VENDA"));
    }

    @Test
    void T12_usuarioInexistente() throws Exception {
        mockMvc.perform(get("/portfolio/naoexiste"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/history/naoexiste"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/buy")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "naoexiste", "symbol", "AAPL", "quantity", 1))))
                .andExpect(status().isNotFound());
    }
}
