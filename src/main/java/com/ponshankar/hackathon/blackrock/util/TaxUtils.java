package com.ponshankar.hackathon.blackrock.util;

public final class TaxUtils {

    private static final double[] SLAB_LIMITS = {700_000, 1_000_000, 1_200_000, 1_500_000};
    private static final double[] SLAB_RATES  = {0.0,     0.10,      0.15,      0.20,  0.30};

    private TaxUtils() {}

    /**
     * Computes income tax using simplified slab rates:
     *   0–7L: 0%, 7L–10L: 10%, 10L–12L: 15%, 12L–15L: 20%, >15L: 30%
     */
    public static double computeTax(double taxableIncome) {
        if (taxableIncome <= 0) return 0.0;

        double tax = 0.0;
        double prev = 0.0;

        for (int i = 0; i < SLAB_LIMITS.length; i++) {
            if (taxableIncome <= SLAB_LIMITS[i]) {
                tax += (taxableIncome - prev) * SLAB_RATES[i];
                return tax;
            }
            tax += (SLAB_LIMITS[i] - prev) * SLAB_RATES[i];
            prev = SLAB_LIMITS[i];
        }

        tax += (taxableIncome - prev) * SLAB_RATES[SLAB_RATES.length - 1];
        return tax;
    }

    /**
     * Computes NPS tax benefit: min(invested, 10% of annualIncome, 200000).
     * Returns the tax saved (difference in tax with and without the deduction).
     */
    public static double npsTaxBenefit(double invested, double annualIncome) {
        if (invested <= 0 || annualIncome <= 0) return 0.0;

        double deduction = Math.min(invested, Math.min(0.10 * annualIncome, 200_000));
        return computeTax(annualIncome) - computeTax(annualIncome - deduction);
    }
}
