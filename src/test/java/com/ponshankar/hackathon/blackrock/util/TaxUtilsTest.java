/**
 * Test type: Unit test
 * Validation: TaxUtils — income tax slab calculation, NPS tax benefit with deduction caps
 * Command: mvn test -Dtest=TaxUtilsTest
 */
package com.ponshankar.hackathon.blackrock.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaxUtilsTest {

    @Test
    void computeTax_belowFirstSlab() {
        assertEquals(0.0, TaxUtils.computeTax(500_000));
        assertEquals(0.0, TaxUtils.computeTax(700_000));
    }

    @Test
    void computeTax_inSecondSlab() {
        // 8L: 0 on first 7L + 10% on 1L = 10,000
        assertEquals(10_000.0, TaxUtils.computeTax(800_000));
    }

    @Test
    void computeTax_atSecondSlabBoundary() {
        // 10L: 0 on 7L + 10% on 3L = 30,000
        assertEquals(30_000.0, TaxUtils.computeTax(1_000_000));
    }

    @Test
    void computeTax_inThirdSlab() {
        // 11L: 30,000 + 15% on 1L = 30,000 + 15,000 = 45,000
        assertEquals(45_000.0, TaxUtils.computeTax(1_100_000));
    }

    @Test
    void computeTax_inFourthSlab() {
        // 13L: 30,000 + 30,000 + 20% on 1L = 80,000
        // 12L boundary: 30,000 + 15%*2L = 30,000 + 30,000 = 60,000
        // 13L: 60,000 + 20%*1L = 80,000
        assertEquals(80_000.0, TaxUtils.computeTax(1_300_000));
    }

    @Test
    void computeTax_aboveAllSlabs() {
        // 20L: 0 + 30,000 + 30,000 + 60,000 + 30%*5L
        // 15L boundary: 30,000 + 30,000 + 60,000 = 120,000
        // 20L: 120,000 + 30%*5L = 120,000 + 150,000 = 270,000
        assertEquals(270_000.0, TaxUtils.computeTax(2_000_000));
    }

    @Test
    void computeTax_zeroAndNegative() {
        assertEquals(0.0, TaxUtils.computeTax(0));
        assertEquals(0.0, TaxUtils.computeTax(-100_000));
    }

    @Test
    void npsTaxBenefit_basicCase() {
        // wage 10L, invested 50k → deduction = min(50k, 1L, 2L) = 50k
        // benefit = tax(10L) - tax(9.5L) = 30,000 - 25,000 = 5,000
        double benefit = TaxUtils.npsTaxBenefit(50_000, 1_000_000);
        assertEquals(5_000.0, benefit);
    }

    @Test
    void npsTaxBenefit_cappedAtTenPercent() {
        // wage 5L, invested 200k → deduction = min(200k, 50k, 200k) = 50k
        double benefit = TaxUtils.npsTaxBenefit(200_000, 500_000);
        // Both 5L and 4.5L are under 7L slab → tax is 0 for both → benefit = 0
        assertEquals(0.0, benefit);
    }

    @Test
    void npsTaxBenefit_cappedAt200k() {
        // wage 30L, invested 500k → deduction = min(500k, 3L, 200k) = 200k
        double benefit = TaxUtils.npsTaxBenefit(500_000, 3_000_000);
        double expected = TaxUtils.computeTax(3_000_000) - TaxUtils.computeTax(2_800_000);
        assertEquals(expected, benefit);
    }

    @Test
    void npsTaxBenefit_zeroInvested() {
        assertEquals(0.0, TaxUtils.npsTaxBenefit(0, 1_000_000));
    }

    @Test
    void npsTaxBenefit_negativeInputs() {
        assertEquals(0.0, TaxUtils.npsTaxBenefit(-1, 1_000_000));
        assertEquals(0.0, TaxUtils.npsTaxBenefit(50_000, -1));
    }
}
