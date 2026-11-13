package com.valeerp.inventory.service.model;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Quant de inventario: cantidad en (producto, almacén, ubicación, lote[, serie]).
 * La cantidad solo cambia vía {@link InventoryMovement} / {@code applyDelta}.
 */
@Entity
@Table(name = "inventory_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_quant_keys",
                columnNames = {"product_id", "warehouse_id", "internal_location_id", "lot_number", "serial_number"})
})
public class InventoryItem extends SoftDeletable {

    private static final String UNTRACKED_LOT = "-";
    private static final String NO_SERIAL = "";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "internal_location_id", nullable = false)
    private Long internalLocationId;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Reserva de pickings confirmados. No muta on-hand; reduce {@link #availableQuantity()}.
     */
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", nullable = false, length = 3))
    })
    private Money unitCost;

    @Column(name = "lot_number", nullable = false, length = 80)
    private String lotNumber;

    @Column(name = "serial_number", nullable = false, length = 120)
    private String serialNumber;

    private LocalDate expirationDate;

    /** Texto libre legado; preferir {@link #internalLocationId}. */
    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime entryDate;

    protected InventoryItem() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
        if (this.quantity == null) {
            this.quantity = 0;
        }
        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }
        if (this.unitCost == null) {
            this.unitCost = Money.zero(Currency.PEN);
        }
        if (this.serialNumber == null) {
            this.serialNumber = NO_SERIAL;
        }
        if (this.lotNumber == null || this.lotNumber.isBlank()) {
            this.lotNumber = UNTRACKED_LOT;
        }
    }

    public static InventoryItem open(Product product, Warehouse warehouse, Long internalLocationId,
                                     String lotNumber, String serialNumber, Money unitCost) {
        if (product == null) {
            throw new DomainException("El producto es obligatorio");
        }
        if (warehouse == null) {
            throw new DomainException("El almacén es obligatorio");
        }
        if (internalLocationId == null) {
            throw new DomainException("La ubicación interna es obligatoria");
        }
        InventoryItem item = new InventoryItem();
        item.product = product;
        item.warehouse = warehouse;
        item.internalLocationId = internalLocationId;
        item.lotNumber = normalizeLot(lotNumber);
        item.serialNumber = normalizeSerial(serialNumber);
        item.unitCost = unitCost == null ? Money.zero(Currency.PEN) : unitCost;
        item.quantity = 0;
        item.reservedQuantity = 0;
        item.entryDate = LocalDateTime.now();
        return item;
    }

    public int availableQuantity() {
        int onHand = this.quantity == null ? 0 : this.quantity;
        int reserved = this.reservedQuantity == null ? 0 : this.reservedQuantity;
        return onHand - reserved;
    }

    public void reserve(int amount) {
        if (amount <= 0) {
            throw new DomainException("La reserva debe ser mayor que cero");
        }
        if (availableQuantity() < amount) {
            throw new DomainException("Stock disponible insuficiente en el lote " + lotNumber);
        }
        this.reservedQuantity = (this.reservedQuantity == null ? 0 : this.reservedQuantity) + amount;
    }

    public void releaseReservation(int amount) {
        if (amount <= 0) {
            throw new DomainException("La liberación de reserva debe ser mayor que cero");
        }
        int reserved = this.reservedQuantity == null ? 0 : this.reservedQuantity;
        if (reserved < amount) {
            throw new DomainException("No hay reserva suficiente en el lote " + lotNumber);
        }
        this.reservedQuantity = reserved - amount;
    }

    /** Libera reserva y descuenta on-hand; el libro debe registrar el movimiento aparte. */
    public void consumeReserved(int amount) {
        releaseReservation(amount);
        applyDelta(-amount);
    }

    public void applyDelta(int delta) {
        applyDelta(delta, false);
    }

    /**
     * @param allowNegative política de stock (configuración); no hardcodear en servicios
     */
    public void applyDelta(int delta, boolean allowNegative) {
        int current = this.quantity == null ? 0 : this.quantity;
        int next = current + delta;
        if (!allowNegative && next < 0) {
            throw new DomainException("Stock insuficiente en el lote " + lotNumber);
        }
        if (isSerialTracked() && next > 1) {
            throw new DomainException("Un quant serializado no puede superar cantidad 1");
        }
        int reserved = this.reservedQuantity == null ? 0 : this.reservedQuantity;
        if (reserved > next) {
            throw new DomainException("El on-hand no puede quedar por debajo de lo reservado en el lote " + lotNumber);
        }
        this.quantity = next;
    }

    public void relocate(Long internalLocationId) {
        if (internalLocationId == null) {
            throw new DomainException("La ubicación interna es obligatoria");
        }
        this.internalLocationId = internalLocationId;
    }

    public void changeUnitCost(Money unitCost) {
        if (unitCost == null) {
            throw new DomainException("El costo unitario es obligatorio");
        }
        this.unitCost = unitCost;
    }

    public void rescheduleExpiration(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isSerialTracked() {
        return serialNumber != null && !serialNumber.isBlank();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Long getInternalLocationId() {
        return internalLocationId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity == null ? 0 : reservedQuantity;
    }

    public Money getUnitCost() {
        return unitCost;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public String getSerialNumber() {
        return serialNumber == null || serialNumber.isBlank() ? null : serialNumber;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public static String normalizeLot(String lotNumber) {
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new DomainException("El movimiento debe nombrar el lote; no se elige uno al azar");
        }
        return lotNumber.trim().toUpperCase();
    }

    public static String normalizeSerial(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return NO_SERIAL;
        }
        return serialNumber.trim().toUpperCase();
    }
}
