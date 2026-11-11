package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Variante de producto (SKU propio). Hijo lógico de {@link Product}; sin cascada SQL desde el padre.
 * <p>
 * Unicidad de SKU vigente:
 * {@code CREATE UNIQUE INDEX uq_product_variants_sku_active ON product_variants (sku) WHERE deleted_at IS NULL;}
 */
@Entity
@Table(name = "product_variants")
public class ProductVariant extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(length = 64)
    private String barcode;

    @Column(length = 200)
    private String label;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantAttribute> attributes = new ArrayList<>();

    protected ProductVariant() {
    }

    private ProductVariant(Long productId, String sku, String barcode, String label) {
        this.productId = productId;
        this.sku = sku;
        this.barcode = barcode;
        this.label = label;
    }

    public static ProductVariant open(Long productId, String sku, String label) {
        return open(productId, sku, null, label);
    }

    public static ProductVariant open(Long productId, String sku, String barcode, String label) {
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        requireSku(sku);
        return new ProductVariant(
                productId,
                sku.trim().toUpperCase(),
                normalizeBarcode(barcode),
                blankToNull(label)
        );
    }

    public void rename(String sku, String label) {
        changeSku(sku);
        this.label = blankToNull(label);
    }

    public void changeSku(String sku) {
        requireSku(sku);
        this.sku = sku.trim().toUpperCase();
    }

    public void changeBarcode(String barcode) {
        this.barcode = normalizeBarcode(barcode);
    }

    public void replaceAttributes(List<ProductVariantAttribute> next) {
        this.attributes.clear();
        if (next == null) {
            return;
        }
        for (ProductVariantAttribute attribute : next) {
            attribute.attachTo(this);
            this.attributes.add(attribute);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLabel() {
        return label;
    }

    /** Compatibilidad con vistas que usaban attributeLabel. */
    public String getAttributeLabel() {
        return label;
    }

    public List<ProductVariantAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    private static void requireSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new DomainException("El SKU de la variante es obligatorio");
        }
    }

    private static String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }
        return barcode.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
