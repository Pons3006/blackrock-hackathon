package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.FilterRequest;
import com.ponshankar.hackathon.blackrock.model.response.FilterResponse;
import com.ponshankar.hackathon.blackrock.util.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionFilterService {

    public FilterResponse filter(FilterRequest request) {
        validatePeriods("q", request.q());
        validatePeriods("p", request.p());
        validatePeriods("k", request.k());
        validateKPeriodsWithinYear(request.k());

        List<Transaction> transactions = request.transactions();
        if (transactions == null || transactions.isEmpty()) {
            return new FilterResponse(List.of(), List.of());
        }

        List<long[]> kRanges = toEpochRanges(request.k());

        List<Transaction> valid = new ArrayList<>();
        List<Transaction> invalid = new ArrayList<>();

        for (Transaction txn : transactions) {
            Long epoch = TimeUtils.toEpochSeconds(txn.date());
            if (epoch != null && isCoveredByAnyK(epoch, kRanges)) {
                valid.add(txn);
            } else {
                invalid.add(new Transaction(
                        txn.date(), txn.amount(), txn.ceiling(), txn.remanent(),
                        "Not covered by any k period"));
            }
        }

        return new FilterResponse(valid, invalid);
    }

    private void validatePeriods(String name, List<Period> periods) {
        if (periods == null) return;
        for (int i = 0; i < periods.size(); i++) {
            Period p = periods.get(i);
            Long start = TimeUtils.toEpochSeconds(p.start());
            Long end = TimeUtils.toEpochSeconds(p.end());
            if (start == null || end == null) {
                throw new IllegalArgumentException(
                        name + "[" + i + "]: invalid timestamp format");
            }
            if (start > end) {
                throw new IllegalArgumentException(
                        name + "[" + i + "]: start must be <= end");
            }
        }
    }

    private void validateKPeriodsWithinYear(List<Period> kPeriods) {
        if (kPeriods == null) return;
        for (int i = 0; i < kPeriods.size(); i++) {
            Period p = kPeriods.get(i);
            LocalDateTime start = LocalDateTime.parse(p.start().trim(), TimeUtils.TIMESTAMP_FORMAT);
            LocalDateTime end = LocalDateTime.parse(p.end().trim(), TimeUtils.TIMESTAMP_FORMAT);
            if (start.getYear() != end.getYear()) {
                throw new IllegalArgumentException(
                        "k[" + i + "]: must not span multiple calendar years");
            }
        }
    }

    private List<long[]> toEpochRanges(List<Period> periods) {
        if (periods == null || periods.isEmpty()) return List.of();
        List<long[]> ranges = new ArrayList<>(periods.size());
        for (Period p : periods) {
            ranges.add(new long[]{
                    TimeUtils.toEpochSeconds(p.start()),
                    TimeUtils.toEpochSeconds(p.end())
            });
        }
        return ranges;
    }

    private boolean isCoveredByAnyK(long epoch, List<long[]> kRanges) {
        for (long[] range : kRanges) {
            if (epoch >= range[0] && epoch <= range[1]) {
                return true;
            }
        }
        return false;
    }
}
