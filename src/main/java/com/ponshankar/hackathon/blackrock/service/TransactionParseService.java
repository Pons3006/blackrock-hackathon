package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Expense;
import com.ponshankar.hackathon.blackrock.model.Totals;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ParseRequest;
import com.ponshankar.hackathon.blackrock.model.response.ParseResponse;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionParseService {

    @Observed(name = "transaction.parse", contextualName = "parse-transactions")
    public ParseResponse parse(ParseRequest request) {
        List<Expense> expenses = request.expenses();
        if (expenses == null || expenses.isEmpty()) {
            return new ParseResponse(List.of(), null);
        }

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
        //Not in requirements but added for convenience
        Totals totals = new Totals(amountSum, ceilingSum, remanentSum);
        return new ParseResponse(transactions, totals);
    }
}
