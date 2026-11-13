package com.valeerp.inventory.stock;

import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.kernel.DomainException;

/**
 * Invariante: todo movimiento del libro nombra lote (y serie si el quant está serializado)
 * y referencia el maestro de trazabilidad.
 */
public final class LotTraceabilityPolicy {

    private LotTraceabilityPolicy() {
    }

    public static void assertQuantReady(InventoryItem quant) {
        if (quant == null) {
            throw new DomainException("El movimiento exige un quant");
        }
        if (quant.getLotNumber() == null || quant.getLotNumber().isBlank()) {
            throw new DomainException("El movimiento debe nombrar el lote; no se elige uno al azar");
        }
        if ("-".equals(quant.getLotNumber())) {
            throw new DomainException("El movimiento exige un número de lote real, no el marcador sin trazabilidad");
        }
    }

    public static void assertMasters(Long stockLotId, InventoryItem quant, Long stockSerialId) {
        assertQuantReady(quant);
        if (stockLotId == null) {
            throw new DomainException("La trazabilidad exige el id del maestro de lote");
        }
        if (quant.isSerialTracked() && stockSerialId == null) {
            throw new DomainException("La trazabilidad de serie exige el id del maestro de serie");
        }
    }
}
