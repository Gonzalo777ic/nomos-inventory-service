package com.nomos.inventory.service.model;

import com.valeerp.catalog.ConversionFactor;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Conversión persistida entre dos UoM de la misma categoría (p. ej. caja↔unidad).
 * Soft-delete; no hay delete físico.
 */
@Entity
@Table(name = "uom_conversions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_uom_conversion_pair", columnNames = {"from_uom_id", "to_uom_id"})
})
public class UomConversion extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_uom_id", nullable = false)
    private Long fromUomId;

    @Column(name = "to_uom_id", nullable = false)
    private Long toUomId;

    @Column(name = "factor", nullable = false, precision = 19, scale = 6)
    private BigDecimal factor;

    protected UomConversion() {
    }

    private UomConversion(Long fromUomId, Long toUomId, BigDecimal factor) {
        this.fromUomId = fromUomId;
        this.toUomId = toUomId;
        this.factor = factor;
    }

    public static UomConversion define(Long fromUomId, Long toUomId, ConversionFactor factor) {
        if (fromUomId == null || toUomId == null) {
            throw new DomainException("Las unidades de origen y destino son obligatorias");
        }
        if (fromUomId.equals(toUomId)) {
            throw new DomainException("La conversión exige unidades distintas");
        }
        if (factor == null) {
            throw new DomainException("El factor de conversión es obligatorio");
        }
        return new UomConversion(fromUomId, toUomId, factor.value());
    }

    public void redefine(ConversionFactor factor) {
        if (factor == null) {
            throw new DomainException("El factor de conversión es obligatorio");
        }
        this.factor = factor.value();
    }

    public ConversionFactor asFactor() {
        return ConversionFactor.of(factor);
    }

    public Long getId() {
        return id;
    }

    public Long getFromUomId() {
        return fromUomId;
    }

    public Long getToUomId() {
        return toUomId;
    }

    public BigDecimal getFactor() {
        return factor;
    }
}
