package com.tcc.finance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenarios T01-T12 do experimento (mesmos nomes nas duas arquiteturas).
 */
@SpringBootTest
@AutoConfigureMockMvc
class FinanceScenariosTest {

    @Autowired
    private MockMvc mvc;

    private static String uniqueUser() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private ResultActions createUser(String username) throws Exception {
        return mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\"}"));
    }

    private ResultActions trade(String op, String username, String symbol, int quantity) throws Exception {
        return mvc.perform(post("/" + op)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"symbol\":\"" + symbol
                        + "\",\"quantity\":" + quantity + "}"));
    }

    @Test
    void T01_cotacaoValida() throws Exception {
        mvc.perform(get("/quote/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.price").value(150.00));
    }

    @Test
    void T02_simboloInvalido() throws Exception {
        mvc.perform(get("/quote/XXXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void T03_criacaoUsuarioSaldoInicial() throws Exception {
        String user = uniqueUser();
        createUser(user)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(user))
                .andExpect(jsonPath("$.saldo").value(10000.00));
    }

    @Test
    void T04_usernameDuplicado() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        createUser(user).andExpect(status().isConflict());
    }

    @Test
    void T05_compraValida() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1500.00))
                .andExpect(jsonPath("$.saldo").value(8500.00));
        mvc.perform(get("/users/" + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(8500.00));
    }

    @Test
    void T06_compraSemSaldoSuficiente() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AMZN", 4) // 13200.00 > 10000.00
                .andExpect(status().isBadRequest());
        mvc.perform(get("/users/" + user))
                .andExpect(jsonPath("$.saldo").value(10000.00));
    }

    @Test
    void T07_quantidadeZero() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 0)
                .andExpect(status().isBadRequest());
    }

    @Test
    void T08_calculoPortfolio() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 10).andExpect(status().isOk()); // 1500.00
        trade("buy", user, "MSFT", 5).andExpect(status().isOk());  // 1500.00
        mvc.perform(get("/portfolio/" + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions", hasSize(2)))
                .andExpect(jsonPath("$.positions[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.positions[0].quantity").value(10))
                .andExpect(jsonPath("$.positions[0].currentPrice").value(150.00))
                .andExpect(jsonPath("$.positions[0].totalValue").value(1500.00))
                .andExpect(jsonPath("$.positions[1].symbol").value("MSFT"))
                .andExpect(jsonPath("$.positions[1].quantity").value(5))
                .andExpect(jsonPath("$.positions[1].totalValue").value(1500.00))
                .andExpect(jsonPath("$.saldo").value(7000.00))
                .andExpect(jsonPath("$.stocksValue").value(3000.00))
                .andExpect(jsonPath("$.totalValue").value(10000.00));
    }

    @Test
    void T09_vendaValida() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 10).andExpect(status().isOk());
        trade("sell", user, "AAPL", 4)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(600.00))
                .andExpect(jsonPath("$.saldo").value(9100.00));
        mvc.perform(get("/portfolio/" + user))
                .andExpect(jsonPath("$.positions[0].quantity").value(6));
    }

    @Test
    void T10_vendaAcimaDaQuantidade() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 2).andExpect(status().isOk());
        trade("sell", user, "AAPL", 3)
                .andExpect(status().isBadRequest());
    }

    @Test
    void T11_historicoOperacoes() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        trade("buy", user, "AAPL", 3).andExpect(status().isOk());
        trade("buy", user, "MSFT", 1).andExpect(status().isOk());
        trade("sell", user, "AAPL", 2).andExpect(status().isOk());
        mvc.perform(get("/history/" + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].type").value("BUY"))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].quantity").value(3))
                .andExpect(jsonPath("$[0].price").value(150.00))
                .andExpect(jsonPath("$[1].type").value("BUY"))
                .andExpect(jsonPath("$[1].symbol").value("MSFT"))
                .andExpect(jsonPath("$[2].type").value("SELL"))
                .andExpect(jsonPath("$[2].symbol").value("AAPL"))
                .andExpect(jsonPath("$[2].quantity").value(2));
    }

    @Test
    void T12_usuarioInexistente() throws Exception {
        String ghost = "inexistente_" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(get("/users/" + ghost)).andExpect(status().isNotFound());
        mvc.perform(get("/portfolio/" + ghost)).andExpect(status().isNotFound());
        mvc.perform(get("/history/" + ghost)).andExpect(status().isNotFound());
        trade("buy", ghost, "AAPL", 1).andExpect(status().isNotFound());
        trade("sell", ghost, "AAPL", 1).andExpect(status().isNotFound());
    }
}
