package com.valeerp.inventory.stock;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Umbrales y políticas de stock desde configuración (no literales en servicios).
 */
@ConfigurationProperties(prefix = "valeerp.inventory.stock")
public class StockQuantitySettings {

    /**
     * Si es false, {@link com.nomos.inventory.service.model.InventoryItem#applyDelta(int, boolean)}
     * rechaza saldo negativo.
     */
    private boolean allowNegative = false;

    /** Días antes del vencimiento para alerta NEAR_EXPIRATION. */
    private int nearExpiryDays = 30;

    /**
     * Stock actual menor o igual a este valor se clasifica como CRITICAL
     * (además de comparar con el umbral del producto).
     */
    private int criticalAbsoluteQty = 0;

    public boolean isAllowNegative() {
        return allowNegative;
    }

    public void setAllowNegative(boolean allowNegative) {
        this.allowNegative = allowNegative;
    }

    public int getNearExpiryDays() {
        return nearExpiryDays;
    }

    public void setNearExpiryDays(int nearExpiryDays) {
        this.nearExpiryDays = nearExpiryDays;
    }

    public int getCriticalAbsoluteQty() {
        return criticalAbsoluteQty;
    }

    public void setCriticalAbsoluteQty(int criticalAbsoluteQty) {
        this.criticalAbsoluteQty = criticalAbsoluteQty;
    }
}
