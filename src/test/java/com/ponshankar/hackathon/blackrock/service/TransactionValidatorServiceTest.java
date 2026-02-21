package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ValidatorRequest;
import com.ponshankar.hackathon.blackrock.model.response.ValidatorResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorServiceTest {

    private final TransactionValidatorService service = new TransactionValidatorService();

    private Transaction validTxn(String date, long amount) {
        long ceiling = ((amount + 99) / 100) * 100;
        return new Transaction(date, amount, ceiling, ceiling - amount);
    }

    @Test
    void validate_allValid() {
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(
                validTxn("2024-01-01 10:00:00", 250),
                validTxn("2024-01-02 10:00:00", 430)
        )));

        assertEquals(2, resp.valid().size());
        assertTrue(resp.invalid().isEmpty());
        assertTrue(resp.duplicate().isEmpty());
    }

    @Test
    void validate_duplicateByDate() {
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(
                validTxn("2024-01-01 10:00:00", 250),
                validTxn("2024-01-01 10:00:00", 430)
        )));

        assertEquals(1, resp.valid().size());
        assertEquals(1, resp.duplicate().size());
        assertEquals(250, resp.valid().get(0).amount());
        assertEquals(430, resp.duplicate().get(0).amount());
    }

    @Test
    void validate_invalidTimestamp() {
        Transaction txn = new Transaction("bad-date", 250L, 300L, 50L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertTrue(resp.valid().isEmpty());
        assertEquals(1, resp.invalid().size());
        assertNotNull(resp.invalid().get(0).message());
    }

    @Test
    void validate_nullTimestamp() {
        Transaction txn = new Transaction(null, 250L, 300L, 50L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
    }

    @Test
    void validate_negativeAmount() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", -1L, 0L, 1L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
        assertTrue(resp.invalid().get(0).message().contains("Amount"));
    }

    @Test
    void validate_amountAtUpperBound() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", 500_000L, 500_000L, 0L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
        assertTrue(resp.invalid().get(0).message().contains("Amount"));
    }

    @Test
    void validate_ceilingNotMultipleOf100() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", 250L, 275L, 25L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
        assertTrue(resp.invalid().get(0).message().contains("Ceiling"));
    }

    @Test
    void validate_ceilingLessThanAmount() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", 250L, 200L, -50L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
    }

    @Test
    void validate_ceilingMinusAmountTooLarge() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", 100L, 300L, 200L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
        assertTrue(resp.invalid().get(0).message().contains("100"));
    }

    @Test
    void validate_remanentMismatch() {
        Transaction txn = new Transaction("2024-01-01 10:00:00", 250L, 300L, 99L);
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, List.of(txn)));

        assertEquals(1, resp.invalid().size());
        assertTrue(resp.invalid().get(0).message().contains("Remanent"));
    }

    @Test
    void validate_negativeWageThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                service.validate(new ValidatorRequest(-1L, List.of())));
    }

    @Test
    void validate_nullWageAllowed() {
        ValidatorResponse resp = service.validate(new ValidatorRequest(null, List.of(
                validTxn("2024-01-01 10:00:00", 250)
        )));
        assertEquals(1, resp.valid().size());
    }

    @Test
    void validate_nullTransactions() {
        ValidatorResponse resp = service.validate(new ValidatorRequest(50_000L, null));
        assertTrue(resp.valid().isEmpty());
        assertTrue(resp.invalid().isEmpty());
        assertTrue(resp.duplicate().isEmpty());
    }
}
