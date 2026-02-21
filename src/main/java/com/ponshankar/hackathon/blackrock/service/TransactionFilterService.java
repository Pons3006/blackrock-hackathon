package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.FilterRequest;
import com.ponshankar.hackathon.blackrock.model.response.FilterResponse;
import com.ponshankar.hackathon.blackrock.util.TimeUtils;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionFilterService {

    private static final Logger log = LoggerFactory.getLogger(TransactionFilterService.class);

    @Observed(name = "transaction.filter", contextualName = "filter-transactions")
    public FilterResponse filter(FilterRequest request) {
        log.debug("Validating period definitions: q={}, p={}, k={}",
                request.q() != null ? request.q().size() : 0,
                request.p() != null ? request.p().size() : 0,
                request.k() != null ? request.k().size() : 0);
        validatePeriods("q", request.q());
        validatePeriods("p", request.p());
        validatePeriods("k", request.k());
        validateKPeriodsWithinYear(request.k());

        List<Transaction> transactions = request.transactions();
        if (transactions == null || transactions.isEmpty()) {
            log.debug("No transactions provided, returning empty response");
            Span.current().setAttribute("transaction.input.count", 0);
            return new FilterResponse(List.of(), List.of());
        }

        log.debug("Filtering {} transactions against k-period coverage", transactions.size());
        List<long[]> kRanges = toEpochRanges(request.k());

        List<Transaction> valid = new ArrayList<>();
        List<Transaction> invalid = new ArrayList<>();
        Set<String> seenDates = new HashSet<>();

        for (Transaction txn : transactions) {
            String reason = validateTransaction(txn);
            if (reason != null) {
                invalid.add(new Transaction(
                        txn.date(), txn.amount(), txn.ceiling(), txn.remanent(), reason));
                continue;
            }
            if (!seenDates.add(txn.date())) {
                invalid.add(new Transaction(
                        txn.date(), txn.amount(), txn.ceiling(), txn.remanent(),
                        "Duplicate transaction"));
                continue;
            }
            Long epoch = TimeUtils.toEpochSeconds(txn.date());
            if (epoch != null && isCoveredByAnyK(epoch, kRanges)) {
                valid.add(txn);
            } else {
                invalid.add(new Transaction(
                        txn.date(), txn.amount(), txn.ceiling(), txn.remanent(),
                        "Not covered by any k period"));
            }
        }

        Span span = Span.current();
        span.setAttribute("transaction.input.count", transactions.size());
        span.setAttribute("transaction.valid.count", valid.size());
        span.setAttribute("transaction.invalid.count", invalid.size());
        span.setAttribute("k.period.count", request.k() != null ? request.k().size() : 0);

        log.info("Filtered {} transactions: valid={}, invalid={}, kPeriods={}",
                transactions.size(), valid.size(), invalid.size(),
                request.k() != null ? request.k().size() : 0);
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
            int startYear = parseYear(p.start().trim(), "k[" + i + "].start");
            int endYear = parseYear(p.end().trim(), "k[" + i + "].end");
            if (startYear != endYear) {
                throw new IllegalArgumentException(
                        "k[" + i + "]: must not span multiple calendar years");
            }
        }
    }

    private int parseYear(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value, TimeUtils.TIMESTAMP_FORMAT).getYear();
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd")).getYear();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException(fieldName + ": invalid date format");
            }
        }
    }

    private static final double MAX_AMOUNT = 500_000;

    private String validateTransaction(Transaction txn) {
        if (txn.date() == null || TimeUtils.toEpochSeconds(txn.date()) == null) {
            return "Invalid or missing timestamp";
        }
        if (txn.amount() == null || txn.amount() < 0 || txn.amount() >= MAX_AMOUNT) {
            return "Amount must be >= 0 and < " + (long) MAX_AMOUNT;
        }
        if (txn.ceiling() == null || txn.ceiling() < 0) {
            return "Ceiling must be non-negative";
        }
        if (txn.remanent() == null || txn.remanent() < 0) {
            return "Remanent must be non-negative";
        }
        return null;
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
