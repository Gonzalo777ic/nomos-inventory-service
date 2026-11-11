package com.valeerp.mrp;

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

/**
 * Línea de consumo teórico (y consumido) de una {@link ManufacturingOrder}.
 * Snapshot al confirmar; no se borra del libro.
 */
@Entity
@Table(name = "mrp_mo_component_lines")
public class MoComponentLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrder manufacturingOrder;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "planned_quantity", nullable = false)
    private int plannedQuantity;

    @Column(name = "consumed_quantity", nullable = false)
    private int consumedQuantity;

    protected MoComponentLine() {
    }

    static MoComponentLine planned(Long productId, int plannedQuantity) {
        if (productId == null) {
            throw new DomainException("La línea de OF exige producto");
        }
        if (plannedQuantity <= 0) {
            throw new DomainException("La cantidad teórica debe ser positiva");
        }
        MoComponentLine line = new MoComponentLine();
        line.productId = productId;
        line.plannedQuantity = plannedQuantity;
        line.consumedQuantity = 0;
        return line;
    }

    void attach(ManufacturingOrder order) {
        this.manufacturingOrder = order;
    }

    void markConsumed() {
        if (consumedQuantity > 0) {
            throw new DomainException("El componente ya fue consumido en esta OF");
        }
        this.consumedQuantity = plannedQuantity;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public int getPlannedQuantity() {
        return plannedQuantity;
    }

    public int getConsumedQuantity() {
        return consumedQuantity;
    }

    public boolean isConsumed() {
        return consumedQuantity > 0;
    }
}
