package com.tcc.finance.market;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenarios do experimento sob responsabilidade do market-service (T01, T02).
 * Mesmos nomes de teste do monolito.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FinanceScenariosTest {

    @Autowired
    private MockMvc mvc;

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
}
