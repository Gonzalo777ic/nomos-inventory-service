package com.valeerp.inventory.stock;

import com.valeerp.kernel.DomainException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Política de dominio para invariantes de cantidad y caducidad.
 * Los umbrales numéricos llegan desde {@link StockQuantitySettings} o del producto.
 */
public final class StockQuantityPolicy {

    private final boolean allowNegative;
    private final int nearExpiryDays;
    private final int criticalAbsoluteQty;

    public StockQuantityPolicy(boolean allowNegative, int nearExpiryDays, int criticalAbsoluteQty) {
        if (nearExpiryDays < 0) {
            throw new DomainException("Los días de alerta de caducidad no pueden ser negativos");
        }
        if (criticalAbsoluteQty < 0) {
            throw new DomainException("El umbral crítico absoluto no puede ser negativo");
        }
        this.allowNegative = allowNegative;
        this.nearExpiryDays = nearExpiryDays;
        this.criticalAbsoluteQty = criticalAbsoluteQty;
    }

    public static StockQuantityPolicy from(StockQuantitySettings settings) {
        Objects.requireNonNull(settings, "settings");
        return new StockQuantityPolicy(
                settings.isAllowNegative(),
                settings.getNearExpiryDays(),
                settings.getCriticalAbsoluteQty()
        );
    }

    public boolean allowsNegative() {
        return allowNegative;
    }

    public int nearExpiryDays() {
        return nearExpiryDays;
    }

    public int criticalAbsoluteQty() {
        return criticalAbsoluteQty;
    }

    public void assertNonNegativeResult(int nextQuantity, String lotNumber) {
        if (!allowNegative && nextQuantity < 0) {
            throw new DomainException("Stock insuficiente en el lote " + lotNumber);
        }
    }

    public boolean isBelowProductThreshold(int onHand, Integer productMinThreshold) {
        if (productMinThreshold == null) {
            return false;
        }
        return onHand <= productMinThreshold;
    }

    public boolean isCritical(int onHand) {
        return onHand <= criticalAbsoluteQty;
    }

    public boolean isExpired(LocalDate expirationDate, LocalDate today) {
        return expirationDate != null && !expirationDate.isAfter(today);
    }

    public boolean isNearExpiry(LocalDate expirationDate, LocalDate today) {
        if (expirationDate == null || isExpired(expirationDate, today)) {
            return false;
        }
        LocalDate horizon = today.plusDays(nearExpiryDays);
        return !expirationDate.isAfter(horizon);
    }
}
