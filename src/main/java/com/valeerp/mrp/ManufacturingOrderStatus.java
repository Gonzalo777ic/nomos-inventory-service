package com.valeerp.mrp;

/**
 * Máquina de estados de la orden de fabricación (libro: no soft-delete).
 * Anulación = {@link #CANCELLED}; no hay delete físico.
 */
public enum ManufacturingOrderStatus {
    DRAFT,
    CONFIRMED,
    IN_PROGRESS,
    DONE,
    CANCELLED
}
