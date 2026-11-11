package com.nomos.inventory.service.model;

import com.valeerp.inventory.OperationType;
import com.valeerp.kernel.DomainException;

/**
 * Alias legado de {@link OperationType}. Preferir OperationType en código nuevo.
 */
public enum MovementType {
    ENTRADA,
    SALIDA_VENTA,
    AJUSTE_DEVOLUCION,
    AJUSTE_PERDIDA,
    TRANSFERENCIA,
    RECEIPT,
    DELIVERY,
    INTERNAL_TRANSFER,
    ADJUSTMENT,
    SCRAP,
    /** Salida compensatoria por devolución a proveedor (libro; anulación = movimiento inverso). */
    DEVOLUCION_PROVEEDOR;

    public OperationType toOperationType() {
        return switch (this) {
            case ENTRADA, RECEIPT -> OperationType.RECEIPT;
            case SALIDA_VENTA, DELIVERY, DEVOLUCION_PROVEEDOR -> OperationType.DELIVERY;
            case TRANSFERENCIA, INTERNAL_TRANSFER -> OperationType.INTERNAL_TRANSFER;
            case AJUSTE_DEVOLUCION, ADJUSTMENT -> OperationType.ADJUSTMENT;
            case AJUSTE_PERDIDA, SCRAP -> OperationType.SCRAP;
        };
    }

    public static MovementType fromOperation(OperationType type) {
        if (type == null) {
            throw new DomainException("El tipo de operación es obligatorio");
        }
        return switch (type) {
            case RECEIPT -> RECEIPT;
            case DELIVERY -> DELIVERY;
            case INTERNAL_TRANSFER -> INTERNAL_TRANSFER;
            case ADJUSTMENT -> ADJUSTMENT;
            case SCRAP -> SCRAP;
        };
    }

    public static MovementType fromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new DomainException("El tipo de movimiento es obligatorio");
        }
        try {
            return MovementType.valueOf(token.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de movimiento desconocido: " + token);
        }
    }
}
