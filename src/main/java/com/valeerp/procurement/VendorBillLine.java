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
@Table(name = "vendor_bill_lines")
public class VendorBillLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_bill_id", nullable = false)
    private VendorBill bill;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_price_currency", nullable = false, length = 3))
    })
    private Money unitPrice;

    protected VendorBillLine() {
    }

    VendorBillLine(VendorBill bill, Long productId, String description, int quantity, Money unitPrice) {
        if (description == null || description.isBlank()) {
            throw new DomainException("La línea de factura exige descripción");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad de la línea debe ser positiva");
        }
        if (unitPrice == null || unitPrice.isNegative()) {
            throw new DomainException("El precio unitario no es válido");
        }
        this.bill = bill;
        this.productId = productId;
        this.description = description.trim();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    void attach(VendorBill bill) {
        this.bill = bill;
    }
}
