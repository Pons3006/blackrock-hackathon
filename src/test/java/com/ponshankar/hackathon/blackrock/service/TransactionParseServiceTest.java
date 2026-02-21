/**
 * Test type: Unit test
 * Validation: TransactionParseService — ceiling calculation, remanent derivation, totals aggregation
 * Command: mvn test -Dtest=TransactionParseServiceTest
 */
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
                new Expense("2024-01-15 10:00:00", 250.0)
        )));

        assertEquals(1, resp.transactions().size());
        Transaction txn = resp.transactions().get(0);
        assertEquals(250.0, txn.amount());
        assertEquals(300.0, txn.ceiling());
        assertEquals(50.0, txn.remanent());
        assertEquals("2024-01-15 10:00:00", txn.date());
    }

    @Test
    void parse_exactMultipleOf100_remanentIsZero() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-15 10:00:00", 500.0)
        )));

        Transaction txn = resp.transactions().get(0);
        assertEquals(500.0, txn.ceiling());
        assertEquals(0.0, txn.remanent());
    }

    @Test
    void parse_amountOne_ceilingIs100() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-15 10:00:00", 1.0)
        )));

        Transaction txn = resp.transactions().get(0);
        assertEquals(100.0, txn.ceiling());
        assertEquals(99.0, txn.remanent());
    }

    @Test
    void parse_totalsAreCorrect() {
        ParseResponse resp = service.parse(new ParseRequest(List.of(
                new Expense("2024-01-01 00:00:00", 250.0),
                new Expense("2024-01-02 00:00:00", 430.0),
                new Expense("2024-01-03 00:00:00", 100.0)
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
                new Expense(ts, 999.0)
        )));
        assertEquals(ts, resp.transactions().get(0).date());
    }
}
