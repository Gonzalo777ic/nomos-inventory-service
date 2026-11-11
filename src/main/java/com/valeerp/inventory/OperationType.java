package com.valeerp.inventory;

/**
 * Tipos de operación de inventario (estilo Odoo) para futuros pickings/transferencias.
 * El libro de movimientos registra cada hecho; la anulación es un movimiento compensatorio.
 */
public enum OperationType {
    RECEIPT,
    DELIVERY,
    INTERNAL_TRANSFER,
    ADJUSTMENT,
    SCRAP
}
