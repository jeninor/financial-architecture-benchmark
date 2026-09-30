package com.juanesteban.tcc.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:postgresql://postgres-test:5432/finance_test",
        "spring.datasource.username=finance_test",
        "spring.datasource.password=finance_test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
@AutoConfigureMockMvc
@Transactional
@Rollback
class FinancialMonolithIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    /*
     * T01
     * Valid stock quote.
     */
    @Test
    void shouldReturnValidQuote()
        throws Exception {

        mockMvc.perform(
                get("/api/quotes/AAPL")
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.symbol")
                    .value("AAPL")
            )
            .andExpect(
                jsonPath("$.name")
                    .value("Apple Inc.")
            )
            .andExpect(
                jsonPath("$.price")
                    .value(200.00)
            );
    }


    /*
     * T02
     * Invalid stock quote.
     */
    @Test
    void shouldRejectInvalidSymbol()
        throws Exception {

        mockMvc.perform(
                get("/api/quotes/INVALID")
            )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                jsonPath("$.status")
                    .value(400)
            );
    }


    /*
     * T03
     * New user starts with $10,000.
     */
    @Test
    void shouldCreateUserWithInitialCash()
        throws Exception {

        mockMvc.perform(
                post("/api/users")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                            "username":
                            "test-user"
                        }
                        """)
            )
            .andExpect(
                status().isCreated()
            )
            .andExpect(
                jsonPath("$.username")
                    .value("test-user")
            )
            .andExpect(
                jsonPath("$.cash")
                    .value(10000.00)
            );
    }


    /*
     * T04
     * Duplicate username should fail.
     */
    @Test
    void shouldRejectDuplicateUsername()
        throws Exception {

        createUser("duplicate-user");

        mockMvc.perform(
                post("/api/users")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                            "username":
                            "duplicate-user"
                        }
                        """)
            )
            .andExpect(
                status().isBadRequest()
            );
    }


    /*
     * T05
     * Valid purchase.
     */
    @Test
    void shouldBuyStock()
        throws Exception {

        String userId =
            createUser("buyer");


        mockMvc.perform(
                post("/api/trades/buy")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"AAPL",
                          "shares":10
                        }
                        """.formatted(userId))
            )
            .andExpect(
                status().isCreated()
            )
            .andExpect(
                jsonPath("$.type")
                    .value("BUY")
            )
            .andExpect(
                jsonPath("$.shares")
                    .value(10)
            )
            .andExpect(
                jsonPath("$.price")
                    .value(200.00)
            )
            .andExpect(
                jsonPath("$.total")
                    .value(2000.00)
            )
            .andExpect(
                jsonPath("$.cashAfter")
                    .value(8000.00)
            );
    }


    /*
     * T06
     * User cannot spend more cash
     * than available.
     */
    @Test
    void shouldRejectPurchaseWithoutEnoughCash()
        throws Exception {

        String userId =
            createUser("poor-buyer");


        mockMvc.perform(
                post("/api/trades/buy")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"AAPL",
                          "shares":100
                        }
                        """.formatted(userId))
            )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                jsonPath("$.error")
                    .value("Insufficient funds")
            );
    }


    /*
     * T07
     * Shares must be positive.
     */
    @Test
    void shouldRejectZeroShares()
        throws Exception {

        String userId =
            createUser("invalid-shares");


        mockMvc.perform(
                post("/api/trades/buy")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"AAPL",
                          "shares":0
                        }
                        """.formatted(userId))
            )
            .andExpect(
                status().isBadRequest()
            );
    }


    /*
     * T08
     * Portfolio calculation.
     */
    @Test
    void shouldCalculatePortfolio()
        throws Exception {

        String userId =
            createUser("portfolio-user");


        buy(
            userId,
            "AAPL",
            10
        );

        buy(
            userId,
            "MSFT",
            5
        );


        mockMvc.perform(
                get(
                    "/api/users/"
                        + userId
                        + "/portfolio"
                )
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath("$.cash")
                    .value(6000.00)
            )
            .andExpect(
                jsonPath("$.holdingsValue")
                    .value(4000.00)
            )
            .andExpect(
                jsonPath("$.totalValue")
                    .value(10000.00)
            )
            .andExpect(
                jsonPath("$.holdings.length()")
                    .value(2)
            );
    }


    /*
     * T09
     * Valid sale credits user cash.
     */
    @Test
    void shouldSellStock()
        throws Exception {

        String userId =
            createUser("seller");


        buy(
            userId,
            "AAPL",
            10
        );


        mockMvc.perform(
                post("/api/trades/sell")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"AAPL",
                          "shares":3
                        }
                        """.formatted(userId))
            )
            .andExpect(
                status().isCreated()
            )
            .andExpect(
                jsonPath("$.type")
                    .value("SELL")
            )
            .andExpect(
                jsonPath("$.shares")
                    .value(3)
            )
            .andExpect(
                jsonPath("$.total")
                    .value(600.00)
            )
            .andExpect(
                jsonPath("$.cashAfter")
                    .value(8600.00)
            );
    }


    /*
     * T10
     * Cannot sell more shares than owned.
     */
    @Test
    void shouldRejectSaleWithoutEnoughShares()
        throws Exception {

        String userId =
            createUser("overseller");


        buy(
            userId,
            "AAPL",
            10
        );


        mockMvc.perform(
                post("/api/trades/sell")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"AAPL",
                          "shares":100
                        }
                        """.formatted(userId))
            )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                jsonPath("$.error")
                    .value(
                        "Insufficient shares. Available: 10"
                    )
            );


        /*
         * Verify failed sale did not alter
         * the existing portfolio.
         */
        mockMvc.perform(
                get(
                    "/api/users/"
                        + userId
                        + "/portfolio"
                )
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath("$.cash")
                    .value(8000.00)
            )
            .andExpect(
                jsonPath(
                    "$.holdings[0].shares"
                )
                    .value(10)
            );
    }


    /*
     * T11
     * Trade history contains BUY and SELL.
     */
    @Test
    void shouldReturnTradeHistory()
        throws Exception {

        String userId =
            createUser("history-user");


        buy(
            userId,
            "AAPL",
            10
        );


        sell(
            userId,
            "AAPL",
            3
        );


        mockMvc.perform(
                get(
                    "/api/users/"
                        + userId
                        + "/trades"
                )
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath("$.length()")
                    .value(2)
            )
            .andExpect(
                jsonPath("$[0].type")
                    .value("BUY")
            )
            .andExpect(
                jsonPath("$[1].type")
                    .value("SELL")
            )
            .andExpect(
                jsonPath("$[1].shares")
                    .value(3)
            );
    }


    /*
     * T12
     * Nonexistent users return 404.
     */
    @Test
    void shouldReturnNotFoundForUnknownUser()
        throws Exception {

        mockMvc.perform(
                get(
                    "/api/users/"
                        + "00000000-0000-0000-0000-000000000001"
                        + "/portfolio"
                )
            )
            .andExpect(
                status().isNotFound()
            );
    }


    /*
     * -----------------------------
     * Test helper methods
     * -----------------------------
     */

    private String createUser(
        String username
    ) throws Exception {

        String response =
            mockMvc.perform(
                    post("/api/users")
                        .contentType(
                            MediaType.APPLICATION_JSON
                        )
                        .content("""
                            {
                              "username":"%s"
                            }
                            """.formatted(username))
                )
                .andExpect(
                    status().isCreated()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();


        JsonNode json =
            objectMapper.readTree(response);


        return json
            .get("id")
            .asText();
    }


    private void buy(
        String userId,
        String symbol,
        int shares
    ) throws Exception {

        mockMvc.perform(
                post("/api/trades/buy")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"%s",
                          "shares":%d
                        }
                        """.formatted(
                            userId,
                            symbol,
                            shares
                        ))
            )
            .andExpect(
                status().isCreated()
            );
    }


    private void sell(
        String userId,
        String symbol,
        int shares
    ) throws Exception {

        mockMvc.perform(
                post("/api/trades/sell")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content("""
                        {
                          "userId":"%s",
                          "symbol":"%s",
                          "shares":%d
                        }
                        """.formatted(
                            userId,
                            symbol,
                            shares
                        ))
            )
            .andExpect(
                status().isCreated()
            );
    }
}