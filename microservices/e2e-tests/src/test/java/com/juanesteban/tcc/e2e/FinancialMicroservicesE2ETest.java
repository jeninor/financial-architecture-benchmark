package com.juanesteban.tcc.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FinancialMicroservicesE2ETest {

    private static final String BASE_URL =
        System.getenv()
            .getOrDefault(
                "BASE_URL",
                "http://localhost:8080"
            );

    private final HttpClient httpClient =
        HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
        new ObjectMapper();


    // =========================================================
    // T01
    // Valid stock quote
    // =========================================================

    @Test
    void shouldReturnValidQuote()
        throws Exception {

        HttpResponse<String> response =
            get(
                "/api/quotes/AAPL"
            );


        assertEquals(
            200,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            "AAPL",
            json.get("symbol").asText()
        );

        assertEquals(
            "Apple Inc.",
            json.get("name").asText()
        );

        assertMoney(
            "200.00",
            json.get("price")
        );
    }


    // =========================================================
    // T02
    // Invalid stock symbol
    // =========================================================

    @Test
    void shouldRejectInvalidSymbol()
        throws Exception {

        HttpResponse<String> response =
            get(
                "/api/quotes/INVALID"
            );


        assertEquals(
            400,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            400,
            json.get("status").asInt()
        );

        assertTrue(
            json
                .get("error")
                .asText()
                .contains(
                    "Invalid stock symbol"
                )
        );
    }


    // =========================================================
    // T03
    // New user starts with $10,000
    // =========================================================

    @Test
    void shouldCreateUserWithInitialCash()
        throws Exception {

        String username =
            uniqueUsername(
                "test-user"
            );


        HttpResponse<String> response =
            post(
                "/api/users",
                """
                {
                  "username":"%s"
                }
                """.formatted(username)
            );


        assertEquals(
            201,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            username,
            json.get("username").asText()
        );

        assertMoney(
            "10000.00",
            json.get("cash")
        );

        assertNotNull(
            json.get("id")
        );
    }


    // =========================================================
    // T04
    // Duplicate username should fail
    // =========================================================

    @Test
    void shouldRejectDuplicateUsername()
        throws Exception {

        String username =
            uniqueUsername(
                "duplicate-user"
            );


        createUser(
            username
        );


        HttpResponse<String> response =
            post(
                "/api/users",
                """
                {
                  "username":"%s"
                }
                """.formatted(username)
            );


        assertEquals(
            400,
            response.statusCode()
        );
    }


    // =========================================================
    // T05
    // Valid stock purchase
    // =========================================================

    @Test
    void shouldBuyStock()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "buyer"
                )
            );


        HttpResponse<String> response =
            buy(
                userId,
                "AAPL",
                10
            );


        assertEquals(
            201,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            "BUY",
            json.get("type").asText()
        );

        assertEquals(
            "AAPL",
            json.get("symbol").asText()
        );

        assertEquals(
            10,
            json.get("shares").asInt()
        );

        assertMoney(
            "200.00",
            json.get("price")
        );

        assertMoney(
            "2000.00",
            json.get("total")
        );

        assertMoney(
            "8000.00",
            json.get("cashAfter")
        );
    }


    // =========================================================
    // T06
    // Purchase without enough cash
    // =========================================================

    @Test
    void shouldRejectPurchaseWithoutEnoughCash()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "poor-buyer"
                )
            );


        HttpResponse<String> response =
            buy(
                userId,
                "AAPL",
                100
            );


        assertEquals(
            400,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            "Insufficient funds",
            json.get("error").asText()
        );


        /*
         * Verify that the failed operation did not
         * alter the financial state.
         */
        HttpResponse<String> portfolioResponse =
            get(
                "/api/users/"
                    + userId
                    + "/portfolio"
            );


        assertEquals(
            200,
            portfolioResponse.statusCode()
        );


        JsonNode portfolio =
            json(
                portfolioResponse
            );


        assertMoney(
            "10000.00",
            portfolio.get("cash")
        );

        assertEquals(
            0,
            portfolio
                .get("holdings")
                .size()
        );
    }


    // =========================================================
    // T07
    // Shares must be greater than zero
    // =========================================================

    @Test
    void shouldRejectZeroShares()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "invalid-shares"
                )
            );


        HttpResponse<String> response =
            buy(
                userId,
                "AAPL",
                0
            );


        assertEquals(
            400,
            response.statusCode()
        );
    }


    // =========================================================
    // T08
    // Portfolio calculation
    // =========================================================

    @Test
    void shouldCalculatePortfolio()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "portfolio-user"
                )
            );


        HttpResponse<String> buyAapl =
            buy(
                userId,
                "AAPL",
                10
            );

        assertEquals(
            201,
            buyAapl.statusCode()
        );


        HttpResponse<String> buyMsft =
            buy(
                userId,
                "MSFT",
                5
            );

        assertEquals(
            201,
            buyMsft.statusCode()
        );


        HttpResponse<String> response =
            get(
                "/api/users/"
                    + userId
                    + "/portfolio"
            );


        assertEquals(
            200,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertMoney(
            "6000.00",
            json.get("cash")
        );

        assertMoney(
            "4000.00",
            json.get("holdingsValue")
        );

        assertMoney(
            "10000.00",
            json.get("totalValue")
        );

        assertEquals(
            2,
            json
                .get("holdings")
                .size()
        );
    }


    // =========================================================
    // T09
    // Valid sale
    // =========================================================

    @Test
    void shouldSellStock()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "seller"
                )
            );


        HttpResponse<String> purchase =
            buy(
                userId,
                "AAPL",
                10
            );

        assertEquals(
            201,
            purchase.statusCode()
        );


        HttpResponse<String> response =
            sell(
                userId,
                "AAPL",
                3
            );


        assertEquals(
            201,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            "SELL",
            json.get("type").asText()
        );

        assertEquals(
            3,
            json.get("shares").asInt()
        );

        assertMoney(
            "600.00",
            json.get("total")
        );


        /*
         * Initial cash:
         * 10000
         *
         * BUY 10 AAPL:
         * -2000
         *
         * SELL 3 AAPL:
         * +600
         *
         * Final cash:
         * 8600
         */
        assertMoney(
            "8600.00",
            json.get("cashAfter")
        );
    }


    // =========================================================
    // T10
    // Cannot sell more shares than owned
    // =========================================================

    @Test
    void shouldRejectSaleWithoutEnoughShares()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "overseller"
                )
            );


        HttpResponse<String> purchase =
            buy(
                userId,
                "AAPL",
                10
            );

        assertEquals(
            201,
            purchase.statusCode()
        );


        HttpResponse<String> response =
            sell(
                userId,
                "AAPL",
                100
            );


        assertEquals(
            400,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            "Insufficient shares. Available: 10",
            json.get("error").asText()
        );


        /*
         * Failed SELL must not alter cash
         * or owned shares.
         */
        HttpResponse<String> portfolioResponse =
            get(
                "/api/users/"
                    + userId
                    + "/portfolio"
            );


        assertEquals(
            200,
            portfolioResponse.statusCode()
        );


        JsonNode portfolio =
            json(
                portfolioResponse
            );


        assertMoney(
            "8000.00",
            portfolio.get("cash")
        );

        assertEquals(
            1,
            portfolio
                .get("holdings")
                .size()
        );

        assertEquals(
            10,
            portfolio
                .get("holdings")
                .get(0)
                .get("shares")
                .asInt()
        );
    }


    // =========================================================
    // T11
    // Trade history
    // =========================================================

    @Test
    void shouldReturnTradeHistory()
        throws Exception {

        String userId =
            createUser(
                uniqueUsername(
                    "history-user"
                )
            );


        HttpResponse<String> purchase =
            buy(
                userId,
                "AAPL",
                10
            );

        assertEquals(
            201,
            purchase.statusCode()
        );


        HttpResponse<String> sale =
            sell(
                userId,
                "AAPL",
                3
            );

        assertEquals(
            201,
            sale.statusCode()
        );


        HttpResponse<String> response =
            get(
                "/api/users/"
                    + userId
                    + "/trades"
            );


        assertEquals(
            200,
            response.statusCode()
        );


        JsonNode json =
            json(response);


        assertEquals(
            2,
            json.size()
        );

        assertEquals(
            "BUY",
            json
                .get(0)
                .get("type")
                .asText()
        );

        assertEquals(
            10,
            json
                .get(0)
                .get("shares")
                .asInt()
        );

        assertEquals(
            "SELL",
            json
                .get(1)
                .get("type")
                .asText()
        );

        assertEquals(
            3,
            json
                .get(1)
                .get("shares")
                .asInt()
        );
    }


    // =========================================================
    // T12
    // Unknown user
    // =========================================================

    @Test
    void shouldReturnNotFoundForUnknownUser()
        throws Exception {

        String unknownUserId =
            UUID
                .randomUUID()
                .toString();


        HttpResponse<String> response =
            get(
                "/api/users/"
                    + unknownUserId
                    + "/portfolio"
            );


        assertEquals(
            404,
            response.statusCode()
        );
    }


    // =========================================================
    // Helper methods
    // =========================================================

    private String createUser(
        String username
    ) throws Exception {

        HttpResponse<String> response =
            post(
                "/api/users",
                """
                {
                  "username":"%s"
                }
                """.formatted(username)
            );


        assertEquals(
            201,
            response.statusCode(),
            "User creation failed: "
                + response.body()
        );


        JsonNode json =
            json(response);


        return json
            .get("id")
            .asText();
    }


    private HttpResponse<String> buy(
        String userId,
        String symbol,
        int shares
    ) throws Exception {

        return post(
            "/api/trades/buy",
            """
            {
              "userId":"%s",
              "symbol":"%s",
              "shares":%d
            }
            """.formatted(
                userId,
                symbol,
                shares
            )
        );
    }


    private HttpResponse<String> sell(
        String userId,
        String symbol,
        int shares
    ) throws Exception {

        return post(
            "/api/trades/sell",
            """
            {
              "userId":"%s",
              "symbol":"%s",
              "shares":%d
            }
            """.formatted(
                userId,
                symbol,
                shares
            )
        );
    }


    private HttpResponse<String> get(
        String path
    )
        throws IOException,
               InterruptedException {

        HttpRequest request =
            HttpRequest
                .newBuilder()
                .uri(
                    URI.create(
                        BASE_URL + path
                    )
                )
                .GET()
                .build();


        return httpClient.send(
            request,
            HttpResponse.BodyHandlers
                .ofString()
        );
    }


    private HttpResponse<String> post(
        String path,
        String body
    )
        throws IOException,
               InterruptedException {

        HttpRequest request =
            HttpRequest
                .newBuilder()
                .uri(
                    URI.create(
                        BASE_URL + path
                    )
                )
                .header(
                    "Content-Type",
                    "application/json"
                )
                .POST(
                    HttpRequest.BodyPublishers
                        .ofString(body)
                )
                .build();


        return httpClient.send(
            request,
            HttpResponse.BodyHandlers
                .ofString()
        );
    }


    private JsonNode json(
        HttpResponse<String> response
    ) throws Exception {

        return objectMapper.readTree(
            response.body()
        );
    }


    private String uniqueUsername(
        String prefix
    ) {

        return prefix
            + "-"
            + UUID
                .randomUUID()
                .toString()
                .substring(
                    0,
                    8
                );
    }


    private void assertMoney(
        String expected,
        JsonNode actual
    ) {

        BigDecimal expectedValue =
            new BigDecimal(
                expected
            );


        BigDecimal actualValue =
            actual.decimalValue();


        assertEquals(
            0,
            expectedValue
                .compareTo(
                    actualValue
                ),
            "Expected monetary value "
                + expectedValue
                + " but got "
                + actualValue
        );
    }
}