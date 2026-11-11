package com.valeerp.inventory.stock;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Maestro de lote. La cantidad física vive en el quant ({@code InventoryItem}), no aquí.
 */
@Entity
@Table(name = "stock_lots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_lot_product_number", columnNames = {"product_id", "lot_number"})
})
public class StockLot extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_number", nullable = false, length = 80)
    private String lotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    protected StockLot() {
    }

    private StockLot(Long productId, String lotNumber, LocalDate expirationDate) {
        this.productId = productId;
        this.lotNumber = lotNumber;
        this.expirationDate = expirationDate;
    }

    public static StockLot open(Long productId, String lotNumber, LocalDate expirationDate) {
        if (productId == null) {
            throw new DomainException("El lote exige productId");
        }
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new DomainException("El número de lote es obligatorio");
        }
        return new StockLot(productId, lotNumber.trim().toUpperCase(), expirationDate);
    }

    public void rescheduleExpiration(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}
