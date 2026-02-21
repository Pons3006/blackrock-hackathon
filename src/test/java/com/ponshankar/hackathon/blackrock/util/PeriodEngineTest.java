/**
 * Test type: Unit test
 * Validation: PeriodEngine utility methods — sortByDate, applyQOverrides, applyPExtras, groupByKPeriods
 * Command: mvn test -Dtest=PeriodEngineTest
 */
package com.ponshankar.hackathon.blackrock.util;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeriodEngineTest {

    // ── helpers ──────────────────────────────────────────────────────────

    private Transaction txn(String date, double remanent) {
        return new Transaction(date, 0.0, 0.0, remanent);
    }

    private Period qPeriod(String start, String end, double fixed) {
        return new Period(start, end, fixed, null);
    }

    private Period pPeriod(String start, String end, double extra) {
        return new Period(start, end, null, extra);
    }

    private Period kPeriod(String start, String end) {
        return new Period(start, end, null, null);
    }

    // ── sortByDate ──────────────────────────────────────────────────────

    @Nested
    class SortByDate {

        @Test
        void sortsTransactionsByTimestamp() {
            var sorted = PeriodEngine.sortByDate(List.of(
                    txn("2024-01-03 00:00:00", 30),
                    txn("2024-01-01 00:00:00", 10),
                    txn("2024-01-02 00:00:00", 20)
            ));

            assertArrayEquals(new double[]{10, 20, 30}, sorted.remanents());
            assertTrue(sorted.epochs()[0] < sorted.epochs()[1]);
            assertTrue(sorted.epochs()[1] < sorted.epochs()[2]);
        }
    }

    // ── applyQOverrides ─────────────────────────────────────────────────

    @Nested
    class ApplyQOverrides {

        @Test
        void singleQOverridesAllCoveredTxns() {
            long[] epochs = {100, 200, 300};
            double[] remanents = {10, 20, 30};

            PeriodEngine.applyQOverrides(epochs, remanents,
                    List.of(qPeriod("1970-01-01 00:00:50", "1970-01-01 00:05:00", 99)));

            assertArrayEquals(new double[]{99, 99, 99}, remanents);
        }

        @Test
        void latestStartWins() {
            long[] epochs = {250};
            double[] remanents = {5};

            PeriodEngine.applyQOverrides(epochs, remanents, List.of(
                    qPeriod("1970-01-01 00:01:40", "1970-01-01 00:08:20", 10),
                    qPeriod("1970-01-01 00:03:20", "1970-01-01 00:08:20", 20)));

            assertEquals(20, remanents[0]);
        }

        @Test
        void tieBreaking_firstInListWins() {
            long[] epochs = {150};
            double[] remanents = {5};

            PeriodEngine.applyQOverrides(epochs, remanents, List.of(
                    qPeriod("1970-01-01 00:01:40", "1970-01-01 00:08:20", 10),
                    qPeriod("1970-01-01 00:01:40", "1970-01-01 00:08:20", 20)));

            assertEquals(10, remanents[0]);
        }

        @Test
        void expiredQDoesNotApply() {
            long[] epochs = {300};
            double[] remanents = {5};

            PeriodEngine.applyQOverrides(epochs, remanents,
                    List.of(qPeriod("1970-01-01 00:01:40", "1970-01-01 00:03:20", 99)));

            assertEquals(5, remanents[0]);
        }

        @Test
        void noQPeriods_unchanged() {
            double[] remanents = {10, 20};
            PeriodEngine.applyQOverrides(new long[]{100, 200}, remanents, List.of());
            assertArrayEquals(new double[]{10, 20}, remanents);
        }

        @Test
        void nullQPeriods_unchanged() {
            double[] remanents = {10};
            PeriodEngine.applyQOverrides(new long[]{100}, remanents, null);
            assertEquals(10, remanents[0]);
        }
    }

    // ── applyPExtras ────────────────────────────────────────────────────

    @Nested
    class ApplyPExtras {

        @Test
        void singlePAddsExtraToAllCovered() {
            long[] epochs = {100, 200, 300};
            double[] remanents = {10, 20, 30};

            PeriodEngine.applyPExtras(epochs, remanents,
                    List.of(pPeriod("1970-01-01 00:00:50", "1970-01-01 00:05:00", 5)));

            assertArrayEquals(new double[]{15, 25, 35}, remanents);
        }

        @Test
        void overlappingPExtrasSum() {
            long[] epochs = {100, 200};
            double[] remanents = {10, 20};

            PeriodEngine.applyPExtras(epochs, remanents, List.of(
                    pPeriod("1970-01-01 00:01:40", "1970-01-01 00:05:00", 5),
                    pPeriod("1970-01-01 00:03:20", "1970-01-01 00:06:40", 3)));

            assertEquals(15, remanents[0]);
            assertEquals(28, remanents[1]);
        }

        @Test
        void expiredPStopsApplying() {
            long[] epochs = {100, 200};
            double[] remanents = {10, 20};

            PeriodEngine.applyPExtras(epochs, remanents,
                    List.of(pPeriod("1970-01-01 00:00:50", "1970-01-01 00:02:30", 5)));

            assertEquals(15, remanents[0]);
            assertEquals(20, remanents[1]);
        }

        @Test
        void noPPeriods_unchanged() {
            double[] remanents = {10};
            PeriodEngine.applyPExtras(new long[]{100}, remanents, List.of());
            assertEquals(10, remanents[0]);
        }

        @Test
        void nullPPeriods_unchanged() {
            double[] remanents = {10};
            PeriodEngine.applyPExtras(new long[]{100}, remanents, null);
            assertEquals(10, remanents[0]);
        }
    }

    // ── groupByKPeriods ─────────────────────────────────────────────────

    @Nested
    class GroupByKPeriods {

        @Test
        void singleKSumsAll() {
            long[] epochs = {100, 200, 300};
            double[] remanents = {10, 20, 30};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents,
                    List.of(kPeriod("1970-01-01 00:00:50", "1970-01-01 00:05:50")));

            assertArrayEquals(new double[]{60}, sums);
        }

        @Test
        void kPartialCoverage() {
            long[] epochs = {100, 200, 300, 400};
            double[] remanents = {10, 20, 30, 40};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents,
                    List.of(kPeriod("1970-01-01 00:02:30", "1970-01-01 00:05:50")));

            assertArrayEquals(new double[]{50}, sums);
        }

        @Test
        void multipleKPeriods() {
            long[] epochs = {100, 200, 300, 400};
            double[] remanents = {10, 20, 30, 40};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents, List.of(
                    kPeriod("1970-01-01 00:01:40", "1970-01-01 00:03:20"),
                    kPeriod("1970-01-01 00:05:00", "1970-01-01 00:06:40")));

            assertEquals(30, sums[0]);
            assertEquals(70, sums[1]);
        }

        @Test
        void txnInMultipleOverlappingK() {
            long[] epochs = {200};
            double[] remanents = {50};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents, List.of(
                    kPeriod("1970-01-01 00:01:40", "1970-01-01 00:05:00"),
                    kPeriod("1970-01-01 00:03:00", "1970-01-01 00:06:40")));

            assertEquals(50, sums[0]);
            assertEquals(50, sums[1]);
        }

        @Test
        void kWithNoTxns() {
            long[] epochs = {100};
            double[] remanents = {10};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents,
                    List.of(kPeriod("1970-01-01 00:05:00", "1970-01-01 00:06:40")));

            assertEquals(0, sums[0]);
        }

        @Test
        void boundaryInclusive() {
            long[] epochs = {100, 200};
            double[] remanents = {10, 20};

            double[] sums = PeriodEngine.groupByKPeriods(epochs, remanents,
                    List.of(kPeriod("1970-01-01 00:01:40", "1970-01-01 00:03:20")));

            assertEquals(30, sums[0]);
        }

        @Test
        void emptyKPeriods() {
            double[] sums = PeriodEngine.groupByKPeriods(
                    new long[]{100}, new double[]{10}, List.of());
            assertEquals(0, sums.length);
        }

        @Test
        void nullKPeriods() {
            double[] sums = PeriodEngine.groupByKPeriods(
                    new long[]{100}, new double[]{10}, null);
            assertEquals(0, sums.length);
        }
    }
}
