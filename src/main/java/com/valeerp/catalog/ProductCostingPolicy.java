package com.valeerp.catalog;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cálculo de coste promedio/estándar. Vive fuera del controlador.
 */
public final class ProductCostingPolicy {

    private ProductCostingPolicy() {
    }

    /**
     * Recalcula el coste promedio ponderado tras una recepción.
     * El coste estándar no cambia con este método.
     */
    public static Money nextAverageCost(Money currentAverage, int onHandQty,
                                        Money incomingUnitCost, int incomingQty) {
        if (incomingQty <= 0) {
            throw new DomainException("La cantidad recibida debe ser positiva");
        }
        if (onHandQty < 0) {
            throw new DomainException("La cantidad en mano no puede ser negativa");
        }
        Money average = currentAverage == null
                ? Money.zero(incomingUnitCost.currency())
                : CatalogMoneySupport.requireNonNegative(currentAverage, "coste promedio");
        Money incoming = CatalogMoneySupport.requireNonNegative(incomingUnitCost, "coste unitario");
        if (!average.currency().equals(incoming.currency())) {
            throw new DomainException("La moneda del coste promedio y del ingreso deben coincidir");
        }
        if (onHandQty == 0) {
            return incoming;
        }
        BigDecimal previousValue = average.amount().multiply(BigDecimal.valueOf(onHandQty));
        BigDecimal incomingValue = incoming.amount().multiply(BigDecimal.valueOf(incomingQty));
        BigDecimal totalQty = BigDecimal.valueOf(onHandQty + (long) incomingQty);
        BigDecimal next = previousValue.add(incomingValue)
                .divide(totalQty, Money.SCALE, RoundingMode.HALF_UP);
        return Money.of(next, incoming.currency());
    }

    public static Money valuationCost(CostingMethod method, Money standardCost, Money averageCost) {
        if (method == null) {
            throw new DomainException("El método de coste es obligatorio");
        }
        return switch (method) {
            case STANDARD -> CatalogMoneySupport.requireNonNegative(
                    standardCost == null ? Money.zero(averageCost == null
                            ? com.valeerp.kernel.Currency.PEN
                            : averageCost.currency()) : standardCost,
                    "coste estándar");
            case AVERAGE -> CatalogMoneySupport.requireNonNegative(
                    averageCost == null ? Money.zero(standardCost == null
                            ? com.valeerp.kernel.Currency.PEN
                            : standardCost.currency()) : averageCost,
                    "coste promedio");
        };
    }
}
