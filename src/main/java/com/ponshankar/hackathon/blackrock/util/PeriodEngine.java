package com.ponshankar.hackathon.blackrock.util;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class PeriodEngine {

    private PeriodEngine() {}

    public record SortedTransactions(long[] epochs, long[] remanents) {}

    /**
     * Parses and sorts transactions by epoch seconds ascending.
     * Returns parallel arrays of epochs and remanents.
     */
    public static SortedTransactions sortByDate(List<Transaction> transactions) {
        int n = transactions.size();

        record Pair(long epoch, long remanent) {}
        Pair[] pairs = new Pair[n];
        for (int i = 0; i < n; i++) {
            Transaction t = transactions.get(i);
            pairs[i] = new Pair(TimeUtils.toEpochSeconds(t.date()), t.remanent());
        }
        Arrays.sort(pairs, Comparator.comparingLong(Pair::epoch));

        long[] epochs = new long[n];
        long[] remanents = new long[n];
        for (int i = 0; i < n; i++) {
            epochs[i] = pairs[i].epoch;
            remanents[i] = pairs[i].remanent;
        }
        return new SortedTransactions(epochs, remanents);
    }

    /**
     * Applies q-period fixed overrides (latest-start wins; ties broken by first in list).
     * Modifies {@code remanents} in place. Both arrays must be sorted by epoch ascending.
     * Complexity: O((n + q) log q).
     */
    public static void applyQOverrides(long[] epochs, long[] remanents, List<Period> qPeriods) {
        if (qPeriods == null || qPeriods.isEmpty()) return;

        int n = epochs.length;
        int q = qPeriods.size();

        record QEntry(long start, long end, long fixed, int index) {}
        QEntry[] entries = new QEntry[q];
        for (int i = 0; i < q; i++) {
            Period p = qPeriods.get(i);
            entries[i] = new QEntry(
                    TimeUtils.toEpochSeconds(p.start()),
                    TimeUtils.toEpochSeconds(p.end()),
                    p.fixed(),
                    i);
        }
        Arrays.sort(entries, Comparator.comparingLong(QEntry::start));

        // max-heap: highest start wins, ties broken by smallest original index
        PriorityQueue<QEntry> heap = new PriorityQueue<>((a, b) ->
                a.start != b.start
                        ? Long.compare(b.start, a.start)
                        : Integer.compare(a.index, b.index));

        int qi = 0;
        for (int ti = 0; ti < n; ti++) {
            long t = epochs[ti];

            while (qi < q && entries[qi].start <= t) {
                heap.offer(entries[qi++]);
            }
            while (!heap.isEmpty() && heap.peek().end < t) {
                heap.poll();
            }
            if (!heap.isEmpty()) {
                remanents[ti] = heap.peek().fixed;
            }
        }
    }

    /**
     * Applies p-period additive extras (sum of all matching).
     * Modifies {@code remanents} in place. Both arrays must be sorted by epoch ascending.
     * Complexity: O((n + p) log p).
     */
    public static void applyPExtras(long[] epochs, long[] remanents, List<Period> pPeriods) {
        if (pPeriods == null || pPeriods.isEmpty()) return;

        int n = epochs.length;
        int p = pPeriods.size();

        record PEntry(long start, long end, long extra) {}
        PEntry[] entries = new PEntry[p];
        for (int i = 0; i < p; i++) {
            Period pd = pPeriods.get(i);
            entries[i] = new PEntry(
                    TimeUtils.toEpochSeconds(pd.start()),
                    TimeUtils.toEpochSeconds(pd.end()),
                    pd.extra());
        }
        Arrays.sort(entries, Comparator.comparingLong(PEntry::start));

        // min-heap by end, so we can efficiently expire the earliest-ending periods
        PriorityQueue<PEntry> activeHeap = new PriorityQueue<>(
                Comparator.comparingLong(PEntry::end));

        long extraSum = 0;
        int pi = 0;

        for (int ti = 0; ti < n; ti++) {
            long t = epochs[ti];

            while (!activeHeap.isEmpty() && activeHeap.peek().end < t) {
                extraSum -= activeHeap.poll().extra;
            }
            while (pi < p && entries[pi].start <= t) {
                activeHeap.offer(entries[pi]);
                extraSum += entries[pi].extra;
                pi++;
            }

            remanents[ti] += extraSum;
        }
    }

    /**
     * Groups adjusted remanents by k periods using prefix sums + binary search.
     * Both arrays must be sorted by epoch ascending.
     * Returns one sum per k period. Complexity: O(n + k log n).
     */
    public static long[] groupByKPeriods(long[] epochs, long[] remanents, List<Period> kPeriods) {
        if (kPeriods == null || kPeriods.isEmpty()) return new long[0];

        int n = epochs.length;
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + remanents[i];
        }

        int k = kPeriods.size();
        long[] sums = new long[k];

        for (int i = 0; i < k; i++) {
            Period p = kPeriods.get(i);
            long start = TimeUtils.toEpochSeconds(p.start());
            long end = TimeUtils.toEpochSeconds(p.end());

            int left = lowerBound(epochs, start);
            int right = upperBound(epochs, end);

            if (left < right) {
                sums[i] = prefix[right] - prefix[left];
            }
        }
        return sums;
    }

    /** First index where epochs[index] >= target. */
    private static int lowerBound(long[] arr, long target) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] < target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    /** First index where epochs[index] > target. */
    private static int upperBound(long[] arr, long target) {
        int lo = 0, hi = arr.length;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arr[mid] <= target) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }
}
