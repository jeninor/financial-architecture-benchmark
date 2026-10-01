package com.tcc.finance.trade;

import com.tcc.finance.trade.client.MarketClient;
import com.tcc.finance.trade.client.UserClient;
import com.tcc.finance.trade.messaging.RabbitConfig;
import com.tcc.finance.trade.messaging.TradeCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cenarios do experimento sob responsabilidade do trade-service (T05-T12).
 * Mesmos nomes de teste do monolito. user-service e market-service sao simulados
 * em memoria (InMemoryRemoteServices) e o RabbitMQ e substituido por um mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FinanceScenariosTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserClient userClient;

    @MockBean
    private MarketClient marketClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private final InMemoryRemoteServices.FakeUserClient users = new InMemoryRemoteServices.FakeUserClient();

    @BeforeEach
    void wireRemoteServices() {
        // Os clientes OpenFeign sao substituidos por mocks que delegam aos fakes em memoria.
        doAnswer(delegatesTo(users)).when(userClient).getUser(any());
        doAnswer(delegatesTo(users)).when(userClient).debit(any(), any());
        doAnswer(delegatesTo(users)).when(userClient).credit(any(), any());
        doAnswer(delegatesTo(InMemoryRemoteServices.marketClient())).when(marketClient).quote(any());
    }

    private String createUser() {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        users.create(username);
        return username;
    }

    private BigDecimal saldo(String username) {
        return users.getUser(username).saldo();
    }

    private ResultActions trade(String op, String username, String symbol, Object quantity) throws Exception {
        return mvc.perform(post("/" + op)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"symbol\":\"" + symbol
                        + "\",\"quantity\":" + quantity + "}"));
    }

    @Test
    void T05_compraValida() throws Exception {
        String user = createUser();
        trade("buy", user, "AAPL", 10)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1500.00))
                .andExpect(jsonPath("$.saldo").value(8500.00));
        assertEquals(0, new BigDecimal("8500.00").compareTo(saldo(user)));
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.TRADE_EXCHANGE),
                eq(RabbitConfig.TRADE_COMPLETED_ROUTING_KEY),
                argThat((Object e) -> e instanceof TradeCompletedEvent ev
                        && ev.username().equals(user) && ev.quantity() == 10));
    }

    @Test
    void T06_compraSemSaldoSuficiente() throws Exception {
        String user = createUser();
        trade("buy", user, "AMZN", 4) // 13200.00 > 10000.00
                .andExpect(status().isBadRequest());
        assertEquals(0, new BigDecimal("10000.00").compareTo(saldo(user)));
    }

    @Test
    void T07_quantidadeZero() throws Exception {
        String user = createUser();
        trade("buy", user, "AAPL", 0)
                .andExpect(status().isBadRequest());
        trade("buy", user, "AAPL", 1.5) // nao inteiro: 400, nao trunca
                .andExpect(status().isBadRequest());
        assertEquals(0, new BigDecimal("10000.00").compareTo(saldo(user)));
    }

    @Test
    void T08_calculoPortfolio() throws Exception {
        String user = createUser();
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
        String user = createUser();
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
        String user = createUser();
        trade("buy", user, "AAPL", 2).andExpect(status().isOk());
        trade("sell", user, "AAPL", 3)
                .andExpect(status().isBadRequest());
        assertEquals(0, new BigDecimal("9700.00").compareTo(saldo(user)));
    }

    @Test
    void T11_historicoOperacoes() throws Exception {
        String user = createUser();
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
        mvc.perform(get("/portfolio/" + ghost)).andExpect(status().isNotFound());
        mvc.perform(get("/history/" + ghost)).andExpect(status().isNotFound());
        trade("buy", ghost, "AAPL", 1).andExpect(status().isNotFound());
        trade("sell", ghost, "AAPL", 1).andExpect(status().isNotFound());
    }
}
