package com.nomos.inventory.service.model;

import com.valeerp.inventory.OperationType;
import com.valeerp.inventory.stock.LotTraceabilityPolicy;
import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Libro de movimientos de inventario. No se oculta con soft-delete;
 * la anulación se registra como movimiento compensatorio.
 * Cada fila lleva trazabilidad de lote/serie (invariante de dominio).
 */
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "internal_location_id")
    private Long internalLocationId;

    @Column(name = "lot_number", nullable = false, length = 80)
    private String lotNumber;

    @Column(name = "serial_number", length = 120)
    private String serialNumber;

    @Column(name = "stock_lot_id", nullable = false)
    private Long stockLotId;

    @Column(name = "stock_serial_id")
    private Long stockSerialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 32)
    private OperationType operationType;

    /**
     * Columna legada para lecturas antiguas; se deriva de {@link #operationType}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32)
    private MovementType type;

    @Column(nullable = false)
    private String reason;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_service")
    private String referenceService;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    protected InventoryMovement() {
    }

    public static InventoryMovement record(InventoryItem quant, int delta, OperationType operationType,
                                           String reason, Long referenceId, String referenceService,
                                           LocalDateTime when, Long stockLotId, Long stockSerialId) {
        LotTraceabilityPolicy.assertMasters(stockLotId, quant, stockSerialId);
        if (delta == 0) {
            throw new DomainException("El movimiento no puede ser cero");
        }
        if (operationType == null) {
            throw new DomainException("El tipo de operación es obligatorio");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("La razón del movimiento es obligatoria");
        }
        if (quant.getWarehouse() == null || quant.getWarehouse().getId() == null) {
            throw new DomainException("El movimiento exige almacén del quant");
        }
        InventoryMovement movement = new InventoryMovement();
        movement.inventoryItem = quant;
        movement.product = quant.getProduct();
        movement.quantityChange = delta;
        movement.balanceAfter = quant.getQuantity();
        movement.warehouseId = quant.getWarehouse().getId();
        movement.internalLocationId = quant.getInternalLocationId();
        movement.lotNumber = quant.getLotNumber();
        movement.serialNumber = quant.getSerialNumber();
        movement.stockLotId = stockLotId;
        movement.stockSerialId = stockSerialId;
        movement.operationType = operationType;
        movement.type = MovementType.fromOperation(operationType);
        movement.reason = reason.trim();
        movement.movementDate = when == null ? LocalDateTime.now() : when;
        movement.referenceId = referenceId;
        movement.referenceService = referenceService;
        movement.cancelled = false;
        return movement;
    }

    public void markCancelled() {
        this.cancelled = true;
    }

    public Long getId() {
        return id;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Long getInternalLocationId() {
        return internalLocationId;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Long getStockLotId() {
        return stockLotId;
    }

    public Long getStockSerialId() {
        return stockSerialId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public MovementType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getReferenceService() {
        return referenceService;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
