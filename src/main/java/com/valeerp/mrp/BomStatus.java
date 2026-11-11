package com.valeerp.mrp;

/**
 * Estado del maestro BOM. La baja lógica ({@code deletedAt}) oculta el registro;
 * ARCHIVED cierra el uso sin borrar histórico de órdenes.
 */
public enum BomStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}
