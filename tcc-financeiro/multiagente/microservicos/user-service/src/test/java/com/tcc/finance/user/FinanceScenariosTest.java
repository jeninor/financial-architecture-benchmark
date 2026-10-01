package com.tcc.finance.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenarios do experimento sob responsabilidade do user-service (T03, T04, T12)
 * e dos endpoints internos de debito/credito (com lock) usados pelo trade-service.
 * Mesmos nomes de teste do monolito.
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

    private ResultActions balance(String op, String username, String amount) throws Exception {
        return mvc.perform(post("/users/" + username + "/" + op)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + amount + "}"));
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
    void T12_usuarioInexistente() throws Exception {
        String ghost = "inexistente_" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(get("/users/" + ghost)).andExpect(status().isNotFound());
        balance("debit", ghost, "1.00").andExpect(status().isNotFound());
        balance("credit", ghost, "1.00").andExpect(status().isNotFound());
    }

    @Test
    void debitoECreditoAtualizamSaldo() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        balance("debit", user, "1500.00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(8500.00));
        balance("credit", user, "600.00")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(9100.00));
        mvc.perform(get("/users/" + user))
                .andExpect(jsonPath("$.saldo").value(9100.00));
    }

    @Test
    void debitoSemSaldoSuficiente() throws Exception {
        String user = uniqueUser();
        createUser(user).andExpect(status().isCreated());
        balance("debit", user, "13200.00").andExpect(status().isBadRequest());
        balance("debit", user, "0").andExpect(status().isBadRequest());
        mvc.perform(get("/users/" + user))
                .andExpect(jsonPath("$.saldo").value(10000.00));
    }
}
