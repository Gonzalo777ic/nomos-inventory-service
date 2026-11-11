package com.valeerp.catalog;

import com.valeerp.kernel.DomainException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Cantidad tipada por unidad de medida (value object de catálogo / inventario).
 */
public final class UomQuantity {

    private final BigDecimal amount;
    private final Long uomId;

    private UomQuantity(BigDecimal amount, Long uomId) {
        this.amount = amount;
        this.uomId = uomId;
    }

    public static UomQuantity of(BigDecimal amount, Long uomId) {
        if (amount == null) {
            throw new DomainException("La cantidad es obligatoria");
        }
        if (amount.signum() < 0) {
            throw new DomainException("La cantidad no puede ser negativa");
        }
        if (uomId == null) {
            throw new DomainException("La unidad de medida es obligatoria");
        }
        return new UomQuantity(amount, uomId);
    }

    public static UomQuantity of(long amount, Long uomId) {
        return of(BigDecimal.valueOf(amount), uomId);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Long uomId() {
        return uomId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UomQuantity that)) {
            return false;
        }
        return amount.compareTo(that.amount) == 0 && Objects.equals(uomId, that.uomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), uomId);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " uom:" + uomId;
    }
}
