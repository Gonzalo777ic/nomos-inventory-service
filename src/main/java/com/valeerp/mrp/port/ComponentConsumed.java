package com.valeerp.mrp.port;

import java.time.Instant;

/**
 * Hecho local: consumo de componente en fabricación.
 * Contrato publicado hacia el libro de inventario vía {@link InventoryMovementPort}.
 * Sin Feign: el adaptador stub solo registra; el futuro adaptador aplicará
 * {@code InventoryMovement} nombrando lote (sin mutar quantity a mano).
 */
public record ComponentConsumed(
        Long manufacturingOrderId,
        Long warehouseId,
        Long productId,
        int quantity,
        Instant occurredAt
) {
}
