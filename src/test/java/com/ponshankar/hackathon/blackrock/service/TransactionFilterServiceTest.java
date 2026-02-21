package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.FilterRequest;
import com.ponshankar.hackathon.blackrock.model.response.FilterResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFilterServiceTest {

    private final TransactionFilterService service = new TransactionFilterService();

    private Transaction txn(String date) {
        return new Transaction(date, 250L, 300L, 50L);
    }

    private Period kPeriod(String start, String end) {
        return new Period(start, end, null, null);
    }

    @Test
    void filter_txnCoveredByK() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59")),
                List.of(txn("2024-01-15 10:00:00"))
        ));

        assertEquals(1, resp.valid().size());
        assertTrue(resp.invalid().isEmpty());
    }

    @Test
    void filter_txnNotCoveredByAnyK() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59")),
                List.of(txn("2024-03-15 10:00:00"))
        ));

        assertTrue(resp.valid().isEmpty());
        assertEquals(1, resp.invalid().size());
        assertEquals("Not covered by any k period", resp.invalid().get(0).message());
    }

    @Test
    void filter_txnCoveredByOneOfMultipleK() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(
                        kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59"),
                        kPeriod("2024-03-01 00:00:00", "2024-03-31 23:59:59")
                ),
                List.of(txn("2024-03-10 12:00:00"))
        ));

        assertEquals(1, resp.valid().size());
        assertTrue(resp.invalid().isEmpty());
    }

    @Test
    void filter_mixOfCoveredAndUncovered() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-06-01 00:00:00", "2024-06-30 23:59:59")),
                List.of(
                        txn("2024-06-15 10:00:00"),
                        txn("2024-07-01 00:00:00"),
                        txn("2024-06-30 23:59:59")
                )
        ));

        assertEquals(2, resp.valid().size());
        assertEquals(1, resp.invalid().size());
    }

    @Test
    void filter_txnOnKBoundary_inclusive() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59")),
                List.of(
                        txn("2024-01-01 00:00:00"),
                        txn("2024-01-31 23:59:59")
                )
        ));

        assertEquals(2, resp.valid().size());
        assertTrue(resp.invalid().isEmpty());
    }

    @Test
    void filter_emptyTransactions() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59")),
                List.of()
        ));

        assertTrue(resp.valid().isEmpty());
        assertTrue(resp.invalid().isEmpty());
    }

    @Test
    void filter_nullTransactions() {
        FilterResponse resp = service.filter(new FilterRequest(
                List.of(), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-01-31 23:59:59")),
                null
        ));

        assertTrue(resp.valid().isEmpty());
        assertTrue(resp.invalid().isEmpty());
    }

    @Test
    void filter_invalidPeriodTimestamp_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.filter(new FilterRequest(
                        List.of(), List.of(),
                        List.of(new Period("bad-date", "2024-01-31 23:59:59", null, null)),
                        List.of()
                )));
    }

    @Test
    void filter_startAfterEnd_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.filter(new FilterRequest(
                        List.of(), List.of(),
                        List.of(kPeriod("2024-01-31 23:59:59", "2024-01-01 00:00:00")),
                        List.of()
                )));
    }

    @Test
    void filter_kSpansMultipleYears_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.filter(new FilterRequest(
                        List.of(), List.of(),
                        List.of(kPeriod("2023-12-01 00:00:00", "2024-01-31 23:59:59")),
                        List.of()
                )));
    }

    @Test
    void filter_invalidQPeriod_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.filter(new FilterRequest(
                        List.of(new Period("bad", "2024-01-31 23:59:59", 100L, null)),
                        List.of(), List.of(), List.of()
                )));
    }

    @Test
    void filter_nullPeriodLists_ok() {
        FilterResponse resp = service.filter(new FilterRequest(
                null, null, null,
                List.of(txn("2024-01-15 10:00:00"))
        ));

        assertTrue(resp.valid().isEmpty());
        assertEquals(1, resp.invalid().size());
    }
}
