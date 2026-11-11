package com.valeerp.catalog;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Tipo de producto estilo Odoo en el contexto Catálogo.
 * No implica mutar cantidad física: el stock vive en Inventario.
 */
public enum ProductType {

    /** Se consume en operaciones; el inventario puede no llevar lote riguroso. */
    CONSUMABLE,

    /** Almacenable: la cantidad solo cambia vía movimientos de inventario. */
    STORABLE,

    /** Servicio: sin stock físico. */
    SERVICE;

    public boolean tracksInventory() {
        return this != SERVICE;
    }

    public static ProductType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("El tipo de producto es obligatorio");
        }
        try {
            return ProductType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Tipo de producto no soportado: " + raw);
        }
    }
}
