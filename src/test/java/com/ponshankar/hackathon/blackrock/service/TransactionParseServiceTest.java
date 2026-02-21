package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Expense;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ParseRequest;
import com.ponshankar.hackathon.blackrock.model.response.ParseResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionParseServiceTest {

    private final TransactionParseService service = new TransactionParseService();

    @Test
    void parse_basicCeilingAndRemanent() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-15 10:00:00", 250L)
        )));

        assertEquals(1, resp.transactions().size());
        Transaction txn = resp.transactions().get(0);
        assertEquals(250L, txn.amount());
        assertEquals(300L, txn.ceiling());
        assertEquals(50L, txn.remanent());
        assertEquals("2024-01-15 10:00:00", txn.date());
    }

    @Test
    void parse_exactMultipleOf100_remanentIsZero() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-15 10:00:00", 500L)
        )));

        Transaction txn = resp.transactions().get(0);
        assertEquals(500L, txn.ceiling());
        assertEquals(0L, txn.remanent());
    }

    @Test
    void parse_amountOne_ceilingIs100() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-15 10:00:00", 1L)
        )));

        Transaction txn = resp.transactions().get(0);
        assertEquals(100L, txn.ceiling());
        assertEquals(99L, txn.remanent());
    }

    @Test
    void parse_totalsAreCorrect() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-01 00:00:00", 250L),
                new Expense("2024-01-02 00:00:00", 430L),
                new Expense("2024-01-03 00:00:00", 100L)
        )));

        assertEquals(3, resp.transactions().size());
        assertNotNull(resp.totals());
        assertEquals(250 + 430 + 100, resp.totals().amountSum());
        assertEquals(300 + 500 + 100, resp.totals().ceilingSum());
        assertEquals(50 + 70 + 0, resp.totals().remanentSum());
    }

    @Test
    void parse_emptyExpenses_returnsEmptyList() {
        ParseResponse resp = service.parse(new ParseRequest(List.of()));
        assertTrue(resp.transactions().isEmpty());
        assertNull(resp.totals());
    }

    @Test
    void parse_nullExpenses_returnsEmptyList() {
        ParseResponse resp = service.parse(new ParseRequest(null));
        assertTrue(resp.transactions().isEmpty());
        assertNull(resp.totals());
    }

    @Test
    void parse_preservesTimestamp() {
        String ts = "2025-12-31 23:59:59";
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense(ts, 999L)
        )));
        assertEquals(ts, resp.transactions().get(0).date());
    }
}
