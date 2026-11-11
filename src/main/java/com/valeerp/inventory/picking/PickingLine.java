package com.valeerp.inventory.picking;

import com.valeerp.inventory.OperationType;
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

@Entity
@Table(name = "picking_lines")
public class PickingLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "picking_id", nullable = false)
    private Picking picking;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    /** Quant/lote nombrado. Obligatorio al confirmar salidas cuando hay ambigüedad. */
    @Column(name = "inventory_item_id")
    private Long inventoryItemId;

    @Column(name = "lot_number", length = 80)
    private String lotNumber;

    @Column(name = "serial_number", length = 120)
    private String serialNumber;

    @Column(name = "source_location_id")
    private Long sourceLocationId;

    @Column(name = "destination_location_id")
    private Long destinationLocationId;

    @Column(name = "reserved", nullable = false)
    private boolean reserved;

    protected PickingLine() {
    }

    static PickingLine open(Picking picking, Long productId, int quantity, Long inventoryItemId,
                            String lotNumber, String serialNumber,
                            Long sourceLocationId, Long destinationLocationId) {
        if (productId == null) {
            throw new DomainException("La línea exige productId");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad de la línea debe ser mayor que cero");
        }
        PickingLine line = new PickingLine();
        line.picking = picking;
        line.productId = productId;
        line.quantity = quantity;
        line.inventoryItemId = inventoryItemId;
        line.lotNumber = blankToNull(lotNumber);
        line.serialNumber = blankToNull(serialNumber);
        line.sourceLocationId = sourceLocationId;
        line.destinationLocationId = destinationLocationId;
        line.reserved = false;
        return line;
    }

    void assertReadyToConfirm(OperationType operationType) {
        if (operationType == OperationType.RECEIPT) {
            if (lotNumber == null || lotNumber.isBlank()) {
                throw new DomainException("La recepción exige lotNumber en cada línea");
            }
            return;
        }
        if (operationType == OperationType.DELIVERY
                || operationType == OperationType.INTERNAL_TRANSFER
                || operationType == OperationType.SCRAP
                || operationType == OperationType.ADJUSTMENT) {
            if (inventoryItemId == null && (lotNumber == null || lotNumber.isBlank())) {
                throw new DomainException(
                        "La línea debe nombrar inventoryItemId o lotNumber; no se elige un lote al azar");
            }
        }
    }

    void bindQuant(Long quantId, String lotNumber) {
        if (quantId == null) {
            throw new DomainException("El quant resuelto es obligatorio");
        }
        this.inventoryItemId = quantId;
        if (lotNumber != null && !lotNumber.isBlank()) {
            this.lotNumber = lotNumber;
        }
    }

    void markReserved() {
        this.reserved = true;
    }

    void clearReserved() {
        this.reserved = false;
    }

    public Long getId() {
        return id;
    }

    public Picking getPicking() {
        return picking;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Long getInventoryItemId() {
        return inventoryItemId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Long getSourceLocationId() {
        return sourceLocationId;
    }

    public Long getDestinationLocationId() {
        return destinationLocationId;
    }

    public boolean isReserved() {
        return reserved;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
