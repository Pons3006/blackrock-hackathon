package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Expense;
import com.ponshankar.hackathon.blackrock.model.Totals;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ParseRequest;
import com.ponshankar.hackathon.blackrock.model.response.ParseResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionParseService {

    public ParseResponse parse(ParseRequest request) {
        List<Expense> expenses = request.expenses();
        if (expenses == null || expenses.isEmpty()) {
            return new ParseResponse(List.of(), null);
        }

        List<Transaction> transactions = new ArrayList<>(expenses.size());
        long amountSum = 0;
        long ceilingSum = 0;
        long remanentSum = 0;

        for (Expense expense : expenses) {
            long amount = expense.amount();
            long ceiling = ((amount + 99) / 100) * 100;
            long remanent = ceiling - amount;

            transactions.add(new Transaction(
                    expense.timestamp(), amount, ceiling, remanent));

            amountSum += amount;
            ceilingSum += ceiling;
            remanentSum += remanent;
        }

        Totals totals = new Totals(amountSum, ceilingSum, remanentSum);
        return new ParseResponse(transactions, totals);
    }
}
