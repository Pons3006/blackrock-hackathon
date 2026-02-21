package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Expense;
import com.ponshankar.hackathon.blackrock.model.Totals;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ParseRequest;
import com.ponshankar.hackathon.blackrock.model.response.ParseResponse;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionParseService {

    private static final Logger log = LoggerFactory.getLogger(TransactionParseService.class);

    @Observed(name = "transaction.parse", contextualName = "parse-transactions")
    public ParseResponse parse(ParseRequest request) {
        List<Expense> expenses = request.expenses();
        if (expenses == null || expenses.isEmpty()) {
            log.debug("No expenses provided, returning empty response");
            Span.current().setAttribute("expense.count", 0);
            Span.current().setAttribute("transaction.count", 0);
            return new ParseResponse(List.of(), null);
        }

        log.debug("Parsing {} expenses into transactions", expenses.size());

        List<Transaction> transactions = new ArrayList<>(expenses.size());
        double amountSum = 0;
        double ceilingSum = 0;
        double remanentSum = 0;

        for (Expense expense : expenses) {
            double amount = expense.amount();
            double ceiling = Math.ceil(amount / 100.0) * 100;
            double remanent = ceiling - amount;

            transactions.add(new Transaction(
                    expense.timestamp(), amount, ceiling, remanent));

            amountSum += amount;
            ceilingSum += ceiling;
            remanentSum += remanent;
        }

        Span span = Span.current();
        span.setAttribute("expense.count", expenses.size());
        span.setAttribute("transaction.count", transactions.size());
        span.setAttribute("total.amount", amountSum);
        span.setAttribute("total.ceiling", ceilingSum);
        span.setAttribute("total.remanent", remanentSum);

        Totals totals = new Totals(amountSum, ceilingSum, remanentSum);
        log.info("Parsed {} expenses: totalAmount={}, totalCeiling={}, totalRemanent={}",
                expenses.size(), amountSum, ceilingSum, remanentSum);
        return new ParseResponse(transactions, totals);
    }
}
