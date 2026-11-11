package com.valeerp.catalog;

import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCostingPolicyTest {

    @Test
    void averageCostIsWeightedByQuantity() {
        Money current = Money.pen(10.00);
        Money incoming = Money.pen(20.00);
        Money next = ProductCostingPolicy.nextAverageCost(current, 3, incoming, 1);
        assertEquals(new BigDecimal("12.50"), next.amount());
        assertEquals("PEN", next.currencyCode());
    }

    @Test
    void valuationUsesStandardWhenConfigured() {
        Money standard = Money.pen(15.00);
        Money average = Money.pen(12.00);
        Money valued = ProductCostingPolicy.valuationCost(CostingMethod.STANDARD, standard, average);
        assertEquals(standard, valued);
    }

    @Test
    void valuationUsesAverageWhenConfigured() {
        Money standard = Money.pen(15.00);
        Money average = Money.pen(12.00);
        Money valued = ProductCostingPolicy.valuationCost(CostingMethod.AVERAGE, standard, average);
        assertEquals(average, valued);
    }
}
