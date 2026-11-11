package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Valor de atributo de una variante (p. ej. attributeId=Talla, value=M).
 * Hijo del agregado {@link ProductVariant}; no se da de baja por separado.
 */
@Entity
@Table(name = "product_variant_attributes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_variant_attribute", columnNames = {"variant_id", "attribute_id"})
})
public class ProductVariantAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @Column(nullable = false, length = 120)
    private String value;

    protected ProductVariantAttribute() {
    }

    private ProductVariantAttribute(Long attributeId, String value) {
        this.attributeId = attributeId;
        this.value = value;
    }

    public static ProductVariantAttribute of(Long attributeId, String value) {
        if (attributeId == null) {
            throw new DomainException("El atributo de la variante es obligatorio");
        }
        if (value == null || value.isBlank()) {
            throw new DomainException("El valor del atributo es obligatorio");
        }
        return new ProductVariantAttribute(attributeId, value.trim());
    }

    void attachTo(ProductVariant owner) {
        if (owner == null) {
            throw new DomainException("La variante dueña es obligatoria");
        }
        this.variant = owner;
    }

    public Long getId() {
        return id;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }
}
