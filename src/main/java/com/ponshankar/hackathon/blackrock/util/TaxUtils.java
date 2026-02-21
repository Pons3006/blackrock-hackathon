package com.ponshankar.hackathon.blackrock.util;

public final class TaxUtils {

    private TaxUtils() {}

    /**
     * Computes income tax using simplified slab rates:
     *   0–7L: 0%, 7L–10L: 10%, 10L–12L: 15%, 12L–15L: 20%, >15L: 30%
     */
    public static double computeTax(double taxableIncome) {
        // TODO: implement slab-based tax calculation
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Computes NPS tax benefit: min(invested, 10% of annualIncome, 200000)
     */
    public static double npsTaxBenefit(double invested, double annualIncome) {
        // TODO: implement NPS Section 80CCD(1B) deduction
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
