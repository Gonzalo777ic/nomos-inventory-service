package com.valeerp.procurement;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_return_lines")
public class VendorReturnLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_return_id", nullable = false)
    private VendorReturn vendorReturn;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", nullable = false, length = 3))
    })
    private Money unitCost;

    protected VendorReturnLine() {
    }

    VendorReturnLine(VendorReturn vendorReturn, Long productId, Long lotId, int quantity, Money unitCost) {
        if (productId == null) {
            throw new DomainException("La línea de devolución exige producto");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad devolvida debe ser positiva");
        }
        if (unitCost == null || unitCost.isNegative()) {
            throw new DomainException("El costo unitario no es válido");
        }
        this.vendorReturn = vendorReturn;
        this.productId = productId;
        this.lotId = lotId;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getLotId() {
        return lotId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Money getUnitCost() {
        return unitCost;
    }
}
