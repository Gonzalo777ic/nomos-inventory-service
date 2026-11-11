package com.valeerp.inventory.reorder;

import com.valeerp.kernel.DomainException;

/**
 * Política de validación de reglas min/max. Sin literales mágicos en servicios/controladores.
 */
public final class StockReorderPolicy {

    private StockReorderPolicy() {
    }

    public static void assertValidBounds(int minQuantity, int maxQuantity) {
        if (minQuantity < 0) {
            throw new DomainException("El mínimo de reabastecimiento no puede ser negativo");
        }
        if (maxQuantity < minQuantity) {
            throw new DomainException("El máximo de reabastecimiento no puede ser menor que el mínimo");
        }
    }

    /** Cantidad sugerida para llevar el on-hand hasta el máximo cuando está bajo el mínimo. */
    public static int suggestedReplenishment(int onHand, int minQuantity, int maxQuantity) {
        assertValidBounds(minQuantity, maxQuantity);
        if (onHand >= minQuantity) {
            return 0;
        }
        return Math.max(0, maxQuantity - onHand);
    }
}
