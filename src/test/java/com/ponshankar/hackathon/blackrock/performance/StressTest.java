/**
 * Test type: Performance / stress test (CI-only — excluded from local runs via "stress" tag)
 * Validation: Sub-second response times with ~10^5 transactions across all major endpoints
 * Command: mvn test -Pstress
 */
package com.ponshankar.hackathon.blackrock.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("stress")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StressTest {

    private static final String BASE = "/blackrock/challenge/v1";
    private static final int N = 100_000;
    private static final Duration MAX_LATENCY = Duration.ofSeconds(1);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ── Helpers ──────────────────────────────────────────────────────────

    private static List<Map<String, Object>> buildExpenses(int count) {
        List<Map<String, Object>> expenses = new ArrayList<>(count);
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        Random rng = new Random(42);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = base.plusMinutes(i);
            double amount = 10 + rng.nextInt(490);
            expenses.add(Map.of(
                    "timestamp", ts.format(FMT),
                    "amount", amount));
        }
        return expenses;
    }

    private static List<Map<String, Object>> buildTransactions(int count) {
        List<Map<String, Object>> txns = new ArrayList<>(count);
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        Random rng = new Random(42);

        for (int i = 0; i < count; i++) {
            LocalDateTime ts = base.plusMinutes(i);
            double amount = 10 + rng.nextInt(490);
            double ceiling = Math.ceil(amount / 100.0) * 100;
            double remanent = ceiling - amount;
            txns.add(Map.of(
                    "date", ts.format(FMT),
                    "amount", amount,
                    "ceiling", ceiling,
                    "remanent", remanent));
        }
        return txns;
    }

    private static Map<String, String> kPeriod2024() {
        return Map.of(
                "start", "2024-01-01 00:00:00",
                "end", "2024-12-31 23:59:59");
    }

    // ── Parse: 10^5 expenses ────────────────────────────────────────────

    @Test
    @Order(1)
    void parse_100kExpenses_completesUnderOneSecond() throws Exception {
        Map<String, Object> body = Map.of("expenses", buildExpenses(N));
        String json = mapper.writeValueAsString(body);

        long start = System.nanoTime();

        mvc.perform(post(BASE + "/transactions:parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(N));

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_LATENCY) < 0,
                "parse took " + elapsed.toMillis() + " ms, exceeds 1 s limit");
    }

    // ── Validator: 10^5 transactions ────────────────────────────────────

    @Test
    @Order(2)
    void validator_100kTransactions_completesUnderOneSecond() throws Exception {
        Map<String, Object> body = Map.of(
                "wage", 50_000,
                "transactions", buildTransactions(N));
        String json = mapper.writeValueAsString(body);

        long start = System.nanoTime();

        mvc.perform(post(BASE + "/transactions:validator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_LATENCY) < 0,
                "validator took " + elapsed.toMillis() + " ms, exceeds 1 s limit");
    }

    // ── Filter: 10^5 transactions ───────────────────────────────────────

    @Test
    @Order(3)
    void filter_100kTransactions_completesUnderOneSecond() throws Exception {
        Map<String, Object> body = Map.of(
                "q", List.of(),
                "p", List.of(),
                "k", List.of(kPeriod2024()),
                "transactions", buildTransactions(N));
        String json = mapper.writeValueAsString(body);

        long start = System.nanoTime();

        mvc.perform(post(BASE + "/transactions:filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_LATENCY) < 0,
                "filter took " + elapsed.toMillis() + " ms, exceeds 1 s limit");
    }

    // ── Returns NPS: 10^5 transactions ──────────────────────────────────

    @Test
    @Order(4)
    void returnsNps_100kTransactions_completesUnderOneSecond() throws Exception {
        Map<String, Object> body = Map.of(
                "age", 30,
                "wage", 100_000,
                "inflation", 0.06,
                "q", List.of(),
                "p", List.of(),
                "k", List.of(kPeriod2024()),
                "transactions", buildTransactions(N));
        String json = mapper.writeValueAsString(body);

        long start = System.nanoTime();

        mvc.perform(post(BASE + "/returns:nps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsByDates").isArray());

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_LATENCY) < 0,
                "returns:nps took " + elapsed.toMillis() + " ms, exceeds 1 s limit");
    }

    // ── Returns Index: 10^5 transactions ────────────────────────────────

    @Test
    @Order(5)
    void returnsIndex_100kTransactions_completesUnderOneSecond() throws Exception {
        Map<String, Object> body = Map.of(
                "age", 30,
                "wage", 100_000,
                "inflation", 0.06,
                "q", List.of(),
                "p", List.of(),
                "k", List.of(kPeriod2024()),
                "transactions", buildTransactions(N));
        String json = mapper.writeValueAsString(body);

        long start = System.nanoTime();

        mvc.perform(post(BASE + "/returns:index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savingsByDates").isArray());

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_LATENCY) < 0,
                "returns:index took " + elapsed.toMillis() + " ms, exceeds 1 s limit");
    }

    // ── Full pipeline: parse → validate → returns (3 sequential calls) ──

    @Test
    @Order(6)
    void fullPipeline_100kExpenses_completesUnderThreeSeconds() throws Exception {
        Map<String, Object> parseBody = Map.of("expenses", buildExpenses(N));
        String parseJson = mapper.writeValueAsString(parseBody);

        long start = System.nanoTime();

        MvcResult parseResult = mvc.perform(post(BASE + "/transactions:parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parseJson))
                .andExpect(status().isOk())
                .andReturn();

        var parsed = mapper.readTree(parseResult.getResponse().getContentAsString());
        var transactions = parsed.get("transactions");

        Map<String, Object> validatorBody = Map.of(
                "wage", 50_000,
                "transactions", mapper.treeToValue(transactions, List.class));

        MvcResult validatorResult = mvc.perform(post(BASE + "/transactions:validator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validatorBody)))
                .andExpect(status().isOk())
                .andReturn();

        var validated = mapper.readTree(validatorResult.getResponse().getContentAsString());
        var validTxns = validated.get("valid");

        Map<String, Object> returnsBody = new LinkedHashMap<>();
        returnsBody.put("age", 30);
        returnsBody.put("wage", 100_000);
        returnsBody.put("inflation", 0.06);
        returnsBody.put("q", List.of());
        returnsBody.put("p", List.of());
        returnsBody.put("k", List.of(kPeriod2024()));
        returnsBody.put("transactions", mapper.treeToValue(validTxns, List.class));

        mvc.perform(post(BASE + "/returns:nps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(returnsBody)))
                .andExpect(status().isOk());

        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        Duration pipelineLimit = MAX_LATENCY.multipliedBy(3);
        assertTrue(elapsed.compareTo(pipelineLimit) < 0,
                "full pipeline took " + elapsed.toMillis() + " ms, exceeds 3 s limit");
    }
}
