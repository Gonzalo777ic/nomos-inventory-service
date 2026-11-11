package com.valeerp.inventory;

import com.valeerp.inventory.location.InternalLocationType;
import com.valeerp.kernel.DomainException;

/**
 * Política de ubicaciones por defecto según el tipo de operación.
 * No hardcodea IDs; solo tipología de ubicación interna del almacén.
 */
public final class OperationTypePolicy {

    private OperationTypePolicy() {
    }

    public static InternalLocationType defaultSource(OperationType type) {
        return switch (require(type)) {
            case RECEIPT -> InternalLocationType.INPUT;
            case DELIVERY -> InternalLocationType.STOCK;
            case INTERNAL_TRANSFER -> InternalLocationType.STOCK;
            case ADJUSTMENT -> InternalLocationType.STOCK;
            case SCRAP -> InternalLocationType.STOCK;
        };
    }

    public static InternalLocationType defaultDestination(OperationType type) {
        return switch (require(type)) {
            case RECEIPT -> InternalLocationType.STOCK;
            case DELIVERY -> InternalLocationType.OUTPUT;
            case INTERNAL_TRANSFER -> InternalLocationType.STOCK;
            case ADJUSTMENT -> InternalLocationType.STOCK;
            case SCRAP -> InternalLocationType.SCRAP;
        };
    }

    public static boolean increasesOnHand(OperationType type) {
        return switch (require(type)) {
            case RECEIPT, ADJUSTMENT -> true;
            case DELIVERY, INTERNAL_TRANSFER, SCRAP -> false;
        };
    }

    private static OperationType require(OperationType type) {
        if (type == null) {
            throw new DomainException("El tipo de operación es obligatorio");
        }
        return type;
    }
}
