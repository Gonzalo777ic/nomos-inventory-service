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

/**
 * Maestro de número de serie. La cantidad del quant asociado es 0 o 1 y solo cambia vía movimiento.
 */
@Entity
@Table(name = "stock_serials", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stock_serial_product_number", columnNames = {"product_id", "serial_number"})
})
public class StockSerial extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "serial_number", nullable = false, length = 120)
    private String serialNumber;

    protected StockSerial() {
    }

    private StockSerial(Long productId, Long lotId, String serialNumber) {
        this.productId = productId;
        this.lotId = lotId;
        this.serialNumber = serialNumber;
    }

    public static StockSerial open(Long productId, Long lotId, String serialNumber) {
        if (productId == null) {
            throw new DomainException("La serie exige productId");
        }
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new DomainException("El número de serie es obligatorio");
        }
        return new StockSerial(productId, lotId, serialNumber.trim().toUpperCase());
    }

    public void assignLot(Long lotId) {
        this.lotId = lotId;
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

    public String getSerialNumber() {
        return serialNumber;
    }
}
