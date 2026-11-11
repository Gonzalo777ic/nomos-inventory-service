package com.valeerp.mrp.port;

import com.valeerp.kernel.Money;

import java.time.Instant;

/**
 * Hecho local: ingreso de producto terminado por fabricación.
 * Ver {@link InventoryMovementPort} y docs/MRP-PORTS.md.
 */
public record FinishedGoodProduced(
        Long manufacturingOrderId,
        Long warehouseId,
        Long productId,
        int quantity,
        Money unitCost,
        Instant occurredAt
) {
}
