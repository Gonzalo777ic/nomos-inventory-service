package com.valeerp.inventory.valuation;

/**
 * Modos de valoración de inventario (estilo Odoo).
 * STUB LOCAL: no publica asientos ni llama a Contabilidad.
 */
public enum InventoryValuationMode {
    /** Valoración al cierre / reporte periódico a partir del on-hand × costo. */
    PERIODIC,
    /** Valoración continua conceptual; este stub solo documenta el modo. */
    PERPETUAL
}
