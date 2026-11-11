package com.valeerp.catalog;

import com.valeerp.kernel.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Factor de conversión: {@code 1 unidad-origen = factor unidades-destino}.
 * Ejemplo caja→unidad: factor 12.
 */
public final class ConversionFactor {

    public static final int SCALE = 6;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal value;

    private ConversionFactor(BigDecimal value) {
        this.value = value.setScale(SCALE, ROUNDING);
    }

    public static ConversionFactor of(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new DomainException("El factor de conversión debe ser positivo");
        }
        return new ConversionFactor(value);
    }

    public static ConversionFactor of(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("El factor de conversión es obligatorio");
        }
        try {
            return of(new BigDecimal(value.trim()));
        } catch (NumberFormatException ex) {
            throw new DomainException("Factor de conversión inválido");
        }
    }

    public ConversionFactor inverse() {
        return new ConversionFactor(BigDecimal.ONE.divide(value, SCALE, ROUNDING));
    }

    public BigDecimal apply(BigDecimal quantity) {
        if (quantity == null) {
            throw new DomainException("La cantidad es obligatoria");
        }
        if (quantity.signum() < 0) {
            throw new DomainException("La cantidad no puede ser negativa");
        }
        return quantity.multiply(value).setScale(SCALE, ROUNDING);
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConversionFactor that)) {
            return false;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
