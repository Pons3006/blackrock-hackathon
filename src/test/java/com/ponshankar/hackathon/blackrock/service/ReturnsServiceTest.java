/**
 * Test type: Unit test
 * Validation: ReturnsService — NPS and index fund compounding, inflation adjustment, tax benefit, q/p overrides
 * Command: mvn test -Dtest=ReturnsServiceTest
 */
package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.SavingsByDate;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ReturnsRequest;
import com.ponshankar.hackathon.blackrock.model.response.ReturnsResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReturnsServiceTest {

    private final ReturnsService service = new ReturnsService();

    private Transaction txn(String date, double amount, double remanent) {
        double ceiling = amount + remanent;
        return new Transaction(date, amount, ceiling, remanent);
    }

    private Period kPeriod(String start, String end) {
        return new Period(start, end, null, null);
    }

    private ReturnsRequest basicRequest(int age, double wage, double inflation,
                                        List<Transaction> txns, List<Period> k) {
        return new ReturnsRequest(age, wage, inflation, List.of(), List.of(), k, txns);
    }

    // ── NPS returns ─────────────────────────────────────────────────────

    @Test
    void nps_basicCompounding() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 50_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        assertEquals(1, resp.savingsByDates().size());
        SavingsByDate s = resp.savingsByDates().get(0);

        assertEquals(50.0, s.amount());
        double expectedNominal = 50.0 * Math.pow(1.0711, 30);
        double expectedProfits = expectedNominal - 50.0;
        assertEquals(expectedProfits, s.profits(), 0.01);
    }

    @Test
    void nps_withInflation() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 50_000.0, 0.06,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        SavingsByDate s = resp.savingsByDates().get(0);
        double nominal = 50.0 * Math.pow(1.0711, 30);
        double real = nominal / Math.pow(1.06, 30);
        double expectedProfits = real - 50.0;
        assertEquals(expectedProfits, s.profits(), 0.01);
    }

    @Test
    void nps_hasTaxBenefit() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 100_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        SavingsByDate s = resp.savingsByDates().get(0);
        assertTrue(s.taxBenefit() >= 0);
    }

    // ── Index returns ───────────────────────────────────────────────────

    @Test
    void index_basicCompounding() {
        ReturnsResponse resp = service.computeIndex(basicRequest(
                30, 50_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        SavingsByDate s = resp.savingsByDates().get(0);
        assertEquals(50.0, s.amount());
        double expectedNominal = 50.0 * Math.pow(1.1449, 30);
        double expectedProfits = expectedNominal - 50.0;
        assertEquals(expectedProfits, s.profits(), 0.01);
    }

    @Test
    void index_taxBenefitIsZero() {
        ReturnsResponse resp = service.computeIndex(basicRequest(
                30, 100_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        assertEquals(0.0, resp.savingsByDates().get(0).taxBenefit());
    }

    // ── Years calculation ───────────────────────────────────────────────

    @Test
    void yearsCalc_ageLessThan60() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                50, 50_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        double expectedNominal = 50.0 * Math.pow(1.0711, 10);
        double expectedProfits = expectedNominal - 50.0;
        assertEquals(expectedProfits, resp.savingsByDates().get(0).profits(), 0.01);
    }

    @Test
    void yearsCalc_age60OrAbove_uses5Years() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                65, 50_000.0, 0.0,
                List.of(txn("2024-01-15 10:00:00", 250, 50)),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        double expectedNominal = 50.0 * Math.pow(1.0711, 5);
        double expectedProfits = expectedNominal - 50.0;
        assertEquals(expectedProfits, resp.savingsByDates().get(0).profits(), 0.01);
    }

    // ── Totals ──────────────────────────────────────────────────────────

    @Test
    void totals_sumOfOriginalTransactions() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 50_000.0, 0.0,
                List.of(
                        txn("2024-01-15 10:00:00", 250, 50),
                        txn("2024-02-15 10:00:00", 430, 70)
                ),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59"))
        ));

        assertEquals(680.0, resp.transactionsTotalAmount());
        assertEquals(800.0, resp.transactionsTotalCeiling());
    }

    // ── q/p integration ─────────────────────────────────────────────────

    @Test
    void qOverride_affectsReturns() {
        Period q = new Period("2024-01-01 00:00:00", "2024-12-31 23:59:59", 100.0, null);
        ReturnsRequest req = new ReturnsRequest(
                30, 50_000.0, 0.0,
                List.of(q), List.of(),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59")),
                List.of(txn("2024-01-15 10:00:00", 250, 50))
        );

        ReturnsResponse resp = service.computeNps(req);
        assertEquals(100.0, resp.savingsByDates().get(0).amount());
    }

    @Test
    void pExtra_affectsReturns() {
        Period p = new Period("2024-01-01 00:00:00", "2024-12-31 23:59:59", null, 25.0);
        ReturnsRequest req = new ReturnsRequest(
                30, 50_000.0, 0.0,
                List.of(), List.of(p),
                List.of(kPeriod("2024-01-01 00:00:00", "2024-12-31 23:59:59")),
                List.of(txn("2024-01-15 10:00:00", 250, 50))
        );

        ReturnsResponse resp = service.computeNps(req);
        assertEquals(75.0, resp.savingsByDates().get(0).amount());
    }

    // ── Edge cases ──────────────────────────────────────────────────────

    @Test
    void emptyTransactions_returnsZeros() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 50_000.0, 0.0, List.of(), List.of()));

        assertEquals(0.0, resp.transactionsTotalAmount());
        assertEquals(0.0, resp.transactionsTotalCeiling());
        assertTrue(resp.savingsByDates().isEmpty());
    }

    @Test
    void nullTransactions_returnsZeros() {
        ReturnsResponse resp = service.computeNps(new ReturnsRequest(
                30, 50_000.0, 0.0, null, null, null, null));

        assertEquals(0.0, resp.transactionsTotalAmount());
        assertTrue(resp.savingsByDates().isEmpty());
    }

    @Test
    void multipleKPeriods_separateResults() {
        ReturnsResponse resp = service.computeNps(basicRequest(
                30, 50_000.0, 0.0,
                List.of(
                        txn("2024-01-15 10:00:00", 250, 50),
                        txn("2024-06-15 10:00:00", 300, 100)
                ),
                List.of(
                        kPeriod("2024-01-01 00:00:00", "2024-03-31 23:59:59"),
                        kPeriod("2024-04-01 00:00:00", "2024-12-31 23:59:59")
                )
        ));

        assertEquals(2, resp.savingsByDates().size());
        assertEquals(50.0, resp.savingsByDates().get(0).amount());
        assertEquals(100.0, resp.savingsByDates().get(1).amount());
    }
}
