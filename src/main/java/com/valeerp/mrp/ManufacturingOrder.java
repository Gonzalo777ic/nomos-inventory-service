package com.valeerp.mrp;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.mrp.BillOfMaterials.TheoreticalNeed;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orden de fabricación (libro). Estados y consumo teórico en el agregado.
 * Consumir/producir no mutan quants aquí: emiten hechos vía puerto (stub).
 */
@Entity
@Table(name = "mrp_manufacturing_orders")
public class ManufacturingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "quantity_to_produce", nullable = false)
    private int quantityToProduce;

    @Column(name = "quantity_produced", nullable = false)
    private int quantityProduced;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ManufacturingOrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", nullable = false, length = 3))
    })
    private Money unitCost;

    @Column(length = 500)
    private String notes;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "manufacturingOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MoComponentLine> componentLines = new ArrayList<>();

    protected ManufacturingOrder() {
    }

    public static ManufacturingOrder draft(String code, Long bomId, Long productId, Long warehouseId,
                                           int quantityToProduce, Money unitCost, String notes) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de la OF es obligatorio");
        }
        if (bomId == null || productId == null || warehouseId == null) {
            throw new DomainException("La OF exige BOM, producto y almacén");
        }
        if (quantityToProduce <= 0) {
            throw new DomainException("La cantidad a fabricar debe ser positiva");
        }
        ManufacturingOrder order = new ManufacturingOrder();
        order.code = code.trim().toUpperCase();
        order.bomId = bomId;
        order.productId = productId;
        order.warehouseId = warehouseId;
        order.quantityToProduce = quantityToProduce;
        order.quantityProduced = 0;
        order.unitCost = unitCost == null ? Money.zero(Currency.PEN) : unitCost;
        order.notes = notes;
        order.status = ManufacturingOrderStatus.DRAFT;
        return order;
    }

    /**
     * Confirma y congela el consumo teórico de componentes (hojas de la BOM).
     */
    public void confirm(List<TheoreticalNeed> theoretical, Instant when) {
        assertStatus(ManufacturingOrderStatus.DRAFT);
        if (theoretical == null || theoretical.isEmpty()) {
            throw new DomainException("La OF exige consumo teórico de componentes");
        }
        if (!componentLines.isEmpty()) {
            throw new DomainException("El consumo teórico ya fue fijado");
        }
        for (TheoreticalNeed need : theoretical) {
            MoComponentLine line = MoComponentLine.planned(need.componentProductId(), need.quantity());
            line.attach(this);
            componentLines.add(line);
        }
        this.status = ManufacturingOrderStatus.CONFIRMED;
        this.confirmedAt = when;
    }

    public void start(Instant when) {
        assertStatus(ManufacturingOrderStatus.CONFIRMED);
        this.status = ManufacturingOrderStatus.IN_PROGRESS;
        this.startedAt = when;
    }

    /**
     * Marca el consumo teórico como ejecutado en el documento.
     * El caso de uso publica hechos stub; no escribe quantity en quants.
     */
    public List<MoComponentLine> consumeComponents() {
        assertStatus(ManufacturingOrderStatus.IN_PROGRESS);
        if (componentLines.isEmpty()) {
            throw new DomainException("La OF no tiene componentes teóricos");
        }
        if (componentsFullyConsumed()) {
            throw new DomainException("Los componentes ya fueron consumidos");
        }
        for (MoComponentLine line : componentLines) {
            line.markConsumed();
        }
        return List.copyOf(componentLines);
    }

    /**
     * Registra producción del terminado en el documento (cantidad objetivo completa).
     */
    public void produceFinished(Instant when) {
        assertStatus(ManufacturingOrderStatus.IN_PROGRESS);
        if (!componentsFullyConsumed()) {
            throw new DomainException("Hay que consumir componentes antes de producir");
        }
        if (quantityProduced > 0) {
            throw new DomainException("La OF ya registró producción");
        }
        this.quantityProduced = quantityToProduce;
        this.status = ManufacturingOrderStatus.DONE;
        this.completedAt = when;
    }

    public void cancel() {
        if (status == ManufacturingOrderStatus.CANCELLED) {
            throw new DomainException("La OF ya está anulada");
        }
        if (status == ManufacturingOrderStatus.DONE) {
            throw new DomainException("No se anula una OF terminada; use reverso de movimientos");
        }
        if (status == ManufacturingOrderStatus.IN_PROGRESS && componentsFullyConsumed()) {
            throw new DomainException("OF en progreso con consumo: anular vía reverso de hechos");
        }
        this.status = ManufacturingOrderStatus.CANCELLED;
    }

    public boolean componentsFullyConsumed() {
        return !componentLines.isEmpty()
                && componentLines.stream().allMatch(MoComponentLine::isConsumed);
    }

    private void assertStatus(ManufacturingOrderStatus expected) {
        if (status != expected) {
            throw new DomainException("La OF debe estar en " + expected + " (actual: " + status + ")");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getBomId() {
        return bomId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public int getQuantityToProduce() {
        return quantityToProduce;
    }

    public int getQuantityProduced() {
        return quantityProduced;
    }

    public ManufacturingOrderStatus getStatus() {
        return status;
    }

    public Money getUnitCost() {
        return unitCost;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public List<MoComponentLine> getComponentLines() {
        return Collections.unmodifiableList(componentLines);
    }
}
