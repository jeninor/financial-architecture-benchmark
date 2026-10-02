package com.tcc.finance.market;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre, no market-service, a parte dos cenarios T01-T12 que pertence a
 * este modulo: T01 (cotacao valida) e T02 (simbolo invalido).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

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
}
