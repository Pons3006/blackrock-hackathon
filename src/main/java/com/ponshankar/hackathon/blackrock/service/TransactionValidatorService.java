package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ValidatorRequest;
import com.ponshankar.hackathon.blackrock.model.response.ValidatorResponse;
import com.ponshankar.hackathon.blackrock.util.TimeUtils;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionValidatorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidatorService.class);

    private static final double MAX_AMOUNT = 500_000;

    @Observed(name = "transaction.validate", contextualName = "validate-transactions")
    public ValidatorResponse validate(ValidatorRequest request) {
        if (request.wage() != null && request.wage() < 0) {
            log.warn("Rejected request: negative wage={}", request.wage());
            throw new IllegalArgumentException("Wage must be non-negative");
        }

        List<Transaction> valid = new ArrayList<>();
        List<Transaction> invalid = new ArrayList<>();
        List<Transaction> duplicate = new ArrayList<>();
        Set<String> seenDates = new HashSet<>();

        List<Transaction> transactions = request.transactions();
        if (transactions == null) {
            log.debug("No transactions provided, returning empty response");
            Span.current().setAttribute("transaction.input.count", 0);
            return new ValidatorResponse(valid, invalid, duplicate);
        }

        log.debug("Validating {} transactions", transactions.size());

        for (Transaction txn : transactions) {
            String reason = validateTransaction(txn);
            if (reason != null) {
                log.trace("Invalid transaction date={}: {}", txn.date(), reason);
                invalid.add(new Transaction(txn.date(), txn.amount(), txn.ceiling(), txn.remanent(), reason));
                continue;
            }
            if (!seenDates.add(txn.date())) {
                log.trace("Duplicate transaction date={}", txn.date());
                duplicate.add(txn);
                continue;
            }
            valid.add(txn);
        }

        Span span = Span.current();
        span.setAttribute("transaction.input.count", transactions.size());
        span.setAttribute("transaction.valid.count", valid.size());
        span.setAttribute("transaction.invalid.count", invalid.size());
        span.setAttribute("transaction.duplicate.count", duplicate.size());

        log.info("Validated {} transactions: valid={}, invalid={}, duplicate={}",
                transactions.size(), valid.size(), invalid.size(), duplicate.size());
        return new ValidatorResponse(valid, invalid, duplicate);
    }

    private String validateTransaction(Transaction txn) {
        if (txn.date() == null || TimeUtils.toEpochSeconds(txn.date()) == null) {
            return "Invalid or missing timestamp";
        }
        if (txn.amount() == null || txn.amount() < 0 || txn.amount() >= MAX_AMOUNT) {
            return "Amount must be >= 0 and < " + (long) MAX_AMOUNT;
        }
        if (txn.ceiling() == null || txn.ceiling() % 100 != 0) {
            return "Ceiling must be a multiple of 100";
        }
        if (txn.ceiling() < txn.amount()) {
            return "Ceiling must be >= amount";
        }
        if (txn.ceiling() - txn.amount() >= 100) {
            return "Ceiling - amount must be < 100";
        }
        if (txn.remanent() == null || Math.abs(txn.remanent() - (txn.ceiling() - txn.amount())) > 1e-9) {
            return "Remanent must equal ceiling - amount";
        }
        return null;
    }
}
