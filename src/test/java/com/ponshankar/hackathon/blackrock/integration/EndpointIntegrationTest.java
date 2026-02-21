/**
 * Test type: Integration test
 * Validation: All REST endpoints — parse, validator, filter, returns (NPS/index), performance
 * Command: mvn test -Dtest=EndpointIntegrationTest
 */
package com.ponshankar.hackathon.blackrock.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndpointIntegrationTest {

    private static final String BASE = "/blackrock/challenge/v1";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ── Parse ───────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void parse_returnsTransactionsWithCeilingAndRemanent() throws Exception {
        String body = """
                {
                  "expenses": [
                    { "timestamp": "2024-01-15 10:30:00", "amount": 250 },
                    { "timestamp": "2024-01-16 14:00:00", "amount": 430 }
                  ]
                }
                """;

        MvcResult result = mvc.perform(post(BASE + "/transactions:parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].ceiling").value(300))
                .andExpect(jsonPath("$.transactions[0].remanent").value(50))
                .andExpect(jsonPath("$.transactions[1].ceiling").value(500))
                .andExpect(jsonPath("$.transactions[1].remanent").value(70))
                .andExpect(jsonPath("$.totals.amountSum").value(680))
                .andExpect(jsonPath("$.totals.ceilingSum").value(800))
                .andExpect(jsonPath("$.totals.remanentSum").value(120))
                .andReturn();
    }

    // ── Validator ───────────────────────────────────────────────────────

    @Test
    @Order(2)
    void validator_categorisesTransactions() throws Exception {
        String body = """
                {
                  "wage": 50000,
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2024-01-15 10:30:00", "amount": 430, "ceiling": 500, "remanent": 70 },
                    { "date": "2024-01-16 14:00:00", "amount": -5,  "ceiling": 0,   "remanent": 5  }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/transactions:validator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(1))
                .andExpect(jsonPath("$.valid[0].amount").value(250))
                .andExpect(jsonPath("$.duplicate.length()").value(1))
                .andExpect(jsonPath("$.duplicate[0].amount").value(430))
                .andExpect(jsonPath("$.invalid.length()").value(1))
                .andExpect(jsonPath("$.invalid[0].message").exists());
    }

    @Test
    @Order(3)
    void validator_negativeWage_returns400() throws Exception {
        String body = """
                { "wage": -1, "transactions": [] }
                """;

        mvc.perform(post(BASE + "/transactions:validator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── Filter ──────────────────────────────────────────────────────────

    @Test
    @Order(4)
    void filter_splitsByCoverage() throws Exception {
        String body = """
                {
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-01-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2024-03-01 12:00:00", "amount": 430, "ceiling": 500, "remanent": 70 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(1))
                .andExpect(jsonPath("$.valid[0].amount").value(250))
                .andExpect(jsonPath("$.invalid.length()").value(1))
                .andExpect(jsonPath("$.invalid[0].message").value("Not covered by any k period"));
    }

    @Test
    @Order(5)
    void filter_invalidPeriod_returns400() throws Exception {
        String body = """
                {
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-31 23:59:59", "end": "2024-01-01 00:00:00" }
                  ],
                  "transactions": []
                }
                """;

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @Order(10)
    void filter_duplicateTransactions_markedInvalid() throws Exception {
        String body = """
                {
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(1))
                .andExpect(jsonPath("$.valid[0].amount").value(250))
                .andExpect(jsonPath("$.invalid.length()").value(1))
                .andExpect(jsonPath("$.invalid[0].message").value("Duplicate transaction"));
    }

    @Test
    @Order(11)
    void filter_negativeAmount_markedInvalid() throws Exception {
        String body = """
                {
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-05-15 10:30:00", "amount": -150, "ceiling": -100, "remanent": -50 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(0))
                .andExpect(jsonPath("$.invalid.length()").value(1))
                .andExpect(jsonPath("$.invalid[0].message").exists());
    }

    @Test
    @Order(12)
    void filter_duplicateAndNegativeMixed_categorisesCorrectly() throws Exception {
        String body = """
                {
                  "q": [
                    { "fixed": 0, "start": "2023-07-01 00:00:00", "end": "2023-07-31 23:59:00" }
                  ],
                  "p": [
                    { "extra": 25, "start": "2023-10-01 08:00:00", "end": "2023-12-31 19:59:00" }
                  ],
                  "k": [
                    { "start": "2023-03-01 00:00:00", "end": "2023-11-30 23:59:00" },
                    { "start": "2023-01-01 00:00:00", "end": "2023-12-31 23:59:00" }
                  ],
                  "transactions": [
                    { "date": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2023-02-28 15:49:00", "amount": 375, "ceiling": 400, "remanent": 25 },
                    { "date": "2023-07-01 21:59:00", "amount": 620, "ceiling": 700, "remanent": 80 },
                    { "date": "2023-12-17 08:09:00", "amount": 480, "ceiling": 500, "remanent": 20 },
                    { "date": "2023-10-12 20:15:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2023-05-15 10:30:00", "amount": -150, "ceiling": -100, "remanent": -50 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid.length()").value(4))
                .andExpect(jsonPath("$.invalid.length()").value(2))
                .andExpect(jsonPath("$.invalid[0].message").exists())
                .andExpect(jsonPath("$.invalid[1].message").exists());
    }

    // ── Returns NPS ─────────────────────────────────────────────────────

    @Test
    @Order(6)
    void returnsNps_computesProfitsAndTaxBenefit() throws Exception {
        String body = """
                {
                  "age": 30,
                  "wage": 100000,
                  "inflation": 0.06,
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 },
                    { "date": "2024-06-15 14:00:00", "amount": 430, "ceiling": 500, "remanent": 70 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/returns:nps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionsTotalAmount").value(680))
                .andExpect(jsonPath("$.transactionsTotalCeiling").value(800))
                .andExpect(jsonPath("$.savingsByDates.length()").value(1))
                .andExpect(jsonPath("$.savingsByDates[0].amount").value(120.0))
                .andExpect(jsonPath("$.savingsByDates[0].profits").isNumber())
                .andExpect(jsonPath("$.savingsByDates[0].taxBenefit").isNumber());
    }

    // ── Returns Index ───────────────────────────────────────────────────

    @Test
    @Order(7)
    void returnsIndex_computesProfitsNoTaxBenefit() throws Exception {
        String body = """
                {
                  "age": 30,
                  "wage": 100000,
                  "inflation": 0.06,
                  "q": [],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/returns:index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsByDates[0].taxBenefit").value(0.0))
                .andExpect(jsonPath("$.savingsByDates[0].profits").isNumber());
    }

    // ── Returns with q/p overrides ──────────────────────────────────────

    @Test
    @Order(8)
    void returnsNps_qOverrideAffectsAmount() throws Exception {
        String body = """
                {
                  "age": 30,
                  "wage": 50000,
                  "inflation": 0.0,
                  "q": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59", "fixed": 200 }
                  ],
                  "p": [],
                  "k": [
                    { "start": "2024-01-01 00:00:00", "end": "2024-12-31 23:59:59" }
                  ],
                  "transactions": [
                    { "date": "2024-01-15 10:30:00", "amount": 250, "ceiling": 300, "remanent": 50 }
                  ]
                }
                """;

        mvc.perform(post(BASE + "/returns:nps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsByDates[0].amount").value(200.0));
    }

    // ── Performance ─────────────────────────────────────────────────────

    @Test
    @Order(9)
    void performance_returnsMetrics() throws Exception {
        mvc.perform(get(BASE + "/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").isString())
                .andExpect(jsonPath("$.memory").isString())
                .andExpect(jsonPath("$.threads").isNumber());
    }
}
