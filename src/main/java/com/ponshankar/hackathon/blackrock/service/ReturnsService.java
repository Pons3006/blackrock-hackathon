package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.SavingsByDate;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.ponshankar.hackathon.blackrock.model.request.ReturnsRequest;
import com.ponshankar.hackathon.blackrock.model.response.ReturnsResponse;
import com.ponshankar.hackathon.blackrock.util.PeriodEngine;
import com.ponshankar.hackathon.blackrock.util.TaxUtils;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnsService {

    private static final double NPS_RATE = 0.0711;
    private static final double INDEX_RATE = 0.1449;

    @Observed(name = "returns.compute-nps", contextualName = "compute-nps-returns")
    public ReturnsResponse computeNps(ReturnsRequest request) {
        Span.current().setAttribute("returns.type", "NPS");
        Span.current().setAttribute("returns.rate", NPS_RATE);
        return compute(request, NPS_RATE, true);
    }

    @Observed(name = "returns.compute-index", contextualName = "compute-index-returns")
    public ReturnsResponse computeIndex(ReturnsRequest request) {
        Span.current().setAttribute("returns.type", "INDEX");
        Span.current().setAttribute("returns.rate", INDEX_RATE);
        return compute(request, INDEX_RATE, false);
    }

    private ReturnsResponse compute(ReturnsRequest request, double rate, boolean nps) {
        Span span = Span.current();
        List<Transaction> transactions = request.transactions();
        if (transactions == null || transactions.isEmpty()) {
            span.setAttribute("transaction.count", 0);
            return new ReturnsResponse(0.0, 0.0, List.of());
        }

        double totalAmount = 0;
        double totalCeiling = 0;
        for (Transaction txn : transactions) {
            totalAmount += txn.amount();
            totalCeiling += txn.ceiling();
        }

        PeriodEngine.SortedTransactions sorted = PeriodEngine.sortByDate(transactions);
        long[] epochs = sorted.epochs();
        double[] remanents = sorted.remanents();

        PeriodEngine.applyQOverrides(epochs, remanents, request.q());
        PeriodEngine.applyPExtras(epochs, remanents, request.p());

        double[] kSums = PeriodEngine.groupByKPeriods(epochs, remanents, request.k());

        int years = (request.age() != null && request.age() < 60)
                ? (60 - request.age()) : 5;
        double inflation = request.inflation() != null ? request.inflation() : 0.0;
        double annualIncome = request.wage() != null ? request.wage() * 12.0 : 0.0;

        List<Period> kPeriods = request.k();
        List<SavingsByDate> savingsByDates = new ArrayList<>(kSums.length);

        for (int i = 0; i < kSums.length; i++) {
            double amount = kSums[i];
            double finalNominal = amount * Math.pow(1 + rate, years);
            double finalReal = finalNominal / Math.pow(1 + inflation, years);
            double profits = finalReal - amount;

            double taxBenefit = 0.0;
            if (nps && annualIncome > 0) {
                taxBenefit = TaxUtils.npsTaxBenefit(amount, annualIncome);
            }

            savingsByDates.add(new SavingsByDate(
                    kPeriods.get(i).start(),
                    kPeriods.get(i).end(),
                    amount,
                    profits,
                    taxBenefit));
        }

        span.setAttribute("transaction.count", transactions.size());
        span.setAttribute("total.amount", totalAmount);
        span.setAttribute("total.ceiling", totalCeiling);
        span.setAttribute("projection.years", years);
        span.setAttribute("projection.inflation", inflation);
        span.setAttribute("k.period.count", kSums.length);

        return new ReturnsResponse(totalAmount, totalCeiling, savingsByDates);
    }
}
