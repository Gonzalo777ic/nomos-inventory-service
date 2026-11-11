package com.valeerp.catalog;

import com.valeerp.kernel.DomainException;

import java.util.Locale;

/**
 * Familia de unidades. Las conversiones solo son válidas dentro de la misma categoría.
 */
public enum UomCategory {

    UNIT,
    WEIGHT,
    VOLUME,
    LENGTH,
    TIME;

    public static UomCategory parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("La categoría de UoM es obligatoria");
        }
        try {
            return UomCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DomainException("Categoría de UoM no soportada: " + raw);
        }
    }
}
