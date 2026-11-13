package com.valeerp.catalog;

import com.valeerp.inventory.service.model.UnitOfMeasure;
import com.valeerp.inventory.service.model.UomConversion;
import com.valeerp.kernel.DomainException;

import java.math.BigDecimal;

/**
 * Política de dominio: convierte cantidades entre UoM usando factores conocidos.
 * Sin literales mágicos en controladores (p. ej. no asumir 12 unidades por caja).
 */
public final class UomConversionPolicy {

    private UomConversionPolicy() {
    }

    public static UomQuantity convert(UomQuantity source,
                                      UnitOfMeasure sourceUom,
                                      UnitOfMeasure targetUom,
                                      UomConversion directOrNull,
                                      UomConversion inverseOrNull) {
        if (source == null) {
            throw new DomainException("La cantidad origen es obligatoria");
        }
        requireSameCategory(sourceUom, targetUom);
        if (!source.uomId().equals(sourceUom.getId())) {
            throw new DomainException("La cantidad no corresponde a la UoM de origen");
        }
        if (sourceUom.getId().equals(targetUom.getId())) {
            return source;
        }
        if (directOrNull != null
                && directOrNull.getFromUomId().equals(sourceUom.getId())
                && directOrNull.getToUomId().equals(targetUom.getId())) {
            BigDecimal converted = directOrNull.asFactor().apply(source.amount());
            return UomQuantity.of(converted, targetUom.getId());
        }
        if (inverseOrNull != null
                && inverseOrNull.getFromUomId().equals(targetUom.getId())
                && inverseOrNull.getToUomId().equals(sourceUom.getId())) {
            BigDecimal converted = inverseOrNull.asFactor().inverse().apply(source.amount());
            return UomQuantity.of(converted, targetUom.getId());
        }
        throw new DomainException("No hay conversión definida entre "
                + sourceUom.getAbbreviation() + " y " + targetUom.getAbbreviation());
    }

    public static void requireSameCategory(UnitOfMeasure left, UnitOfMeasure right) {
        if (left == null || right == null) {
            throw new DomainException("Las unidades de medida son obligatorias");
        }
        if (left.getCategory() != right.getCategory()) {
            throw new DomainException("Solo se convierten UoM de la misma categoría");
        }
    }
}
