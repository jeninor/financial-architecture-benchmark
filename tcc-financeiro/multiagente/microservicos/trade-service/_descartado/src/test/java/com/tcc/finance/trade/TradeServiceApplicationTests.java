package com.tcc.finance.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcc.finance.trade.client.MarketServiceClient;
import com.tcc.finance.trade.client.UserServiceClient;
import com.tcc.finance.trade.dto.AmountRequest;
import com.tcc.finance.trade.dto.QuoteDTO;
import com.tcc.finance.trade.dto.UserDTO;
import com.tcc.finance.trade.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre, no trade-service, a parte dos cenarios T01-T12 que pertence a este
 * modulo: T05-T11 (compra/venda/portfolio/historico) e T12 (usuario
 * inexistente). Como o trade-service depende do user-service e do
 * market-service via OpenFeign, os dois clientes Feign e o RabbitTemplate
 * sao substituidos por @MockBean, para nao exigir os 5 servicos no ar
 * durante `mvn test` (ver AGENTE1_ESPECIFICACAO.md).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradeServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private MarketServiceClient marketServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private String buyBody(String username, String symbol, long quantity) throws Exception {
        return objectMapper.writeValueAsString(Map.of("username", username, "symbol", symbol, "quantity", quantity));
    }

    @Test
    void T05_compraValida() throws Exception {
        when(userServiceClient.getUser("t05user")).thenReturn(new UserDTO("t05user", new BigDecimal("10000.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("AAPL", new BigDecimal("150.00")));
        when(userServiceClient.debit(eq("t05user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t05user", new BigDecimal("8500.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t05user", "AAPL", 10)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(8500.00, 0.001)));
    }

    @Test
    void T06_compraSemSaldoSuficiente() throws Exception {
        when(userServiceClient.getUser("t06user")).thenReturn(new UserDTO("t06user", new BigDecimal("100.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("AMZN", new BigDecimal("3300.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t06user", "AMZN", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T07_quantidadeIgualAZero() throws Exception {
        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t07user", "AAPL", 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T08_calculoDoPortfolio() throws Exception {
        when(userServiceClient.getUser("t08user")).thenReturn(new UserDTO("t08user", new BigDecimal("8500.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("MSFT", new BigDecimal("300.00")));
        when(userServiceClient.debit(eq("t08user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t08user", new BigDecimal("8500.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t08user", "MSFT", 5)))
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
        when(userServiceClient.getUser("t09user")).thenReturn(new UserDTO("t09user", new BigDecimal("10000.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("AAPL", new BigDecimal("150.00")));
        when(userServiceClient.debit(eq("t09user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t09user", new BigDecimal("8500.00")));
        when(userServiceClient.credit(eq("t09user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t09user", new BigDecimal("9100.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t09user", "AAPL", 10)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sell").contentType("application/json").content(buyBody("t09user", "AAPL", 4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(closeTo(9100.00, 0.001)));
    }

    @Test
    void T10_vendaAcimaDaQuantidadeDisponivel() throws Exception {
        when(userServiceClient.getUser("t10user")).thenReturn(new UserDTO("t10user", new BigDecimal("10000.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("AAPL", new BigDecimal("150.00")));
        when(userServiceClient.debit(eq("t10user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t10user", new BigDecimal("9700.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t10user", "AAPL", 2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sell").contentType("application/json").content(buyBody("t10user", "AAPL", 99)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void T11_historicoDeOperacoes() throws Exception {
        when(userServiceClient.getUser("t11user")).thenReturn(new UserDTO("t11user", new BigDecimal("10000.00")));
        when(marketServiceClient.getQuote(anyString())).thenReturn(new QuoteDTO("AAPL", new BigDecimal("150.00")));
        when(userServiceClient.debit(eq("t11user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t11user", new BigDecimal("8500.00")));
        when(userServiceClient.credit(eq("t11user"), any(AmountRequest.class)))
                .thenReturn(new UserDTO("t11user", new BigDecimal("8950.00")));

        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("t11user", "AAPL", 10)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/sell").contentType("application/json").content(buyBody("t11user", "AAPL", 3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/history/t11user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("COMPRA"))
                .andExpect(jsonPath("$[1].tipo").value("VENDA"));
    }

    @Test
    void T12_usuarioInexistente() throws Exception {
        when(userServiceClient.getUser("naoexiste")).thenThrow(new UserNotFoundException("naoexiste"));

        mockMvc.perform(get("/portfolio/naoexiste")).andExpect(status().isNotFound());
        mockMvc.perform(get("/history/naoexiste")).andExpect(status().isNotFound());
        mockMvc.perform(post("/buy").contentType("application/json").content(buyBody("naoexiste", "AAPL", 1)))
                .andExpect(status().isNotFound());
    }
}
