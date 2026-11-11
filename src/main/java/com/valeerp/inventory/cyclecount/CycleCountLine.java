package com.valeerp.inventory.cyclecount;

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
@Table(name = "cycle_count_lines")
public class CycleCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_count_id", nullable = false)
    private CycleCount cycleCount;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Quant/lote explícito — obligatorio; no findFirst entre lotes. */
    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "system_quantity", nullable = false)
    private Integer systemQuantity;

    @Column(name = "counted_quantity")
    private Integer countedQuantity;

    protected CycleCountLine() {
    }

    static CycleCountLine open(CycleCount cycleCount, Long productId, Long inventoryItemId, int systemQuantity) {
        if (productId == null) {
            throw new DomainException("La línea de conteo exige productId");
        }
        if (inventoryItemId == null) {
            throw new DomainException("La línea de conteo debe nombrar el quant/lote");
        }
        if (systemQuantity < 0) {
            throw new DomainException("La cantidad de sistema no puede ser negativa");
        }
        CycleCountLine line = new CycleCountLine();
        line.cycleCount = cycleCount;
        line.productId = productId;
        line.inventoryItemId = inventoryItemId;
        line.systemQuantity = systemQuantity;
        return line;
    }

    public void recordCount(int countedQuantity) {
        if (countedQuantity < 0) {
            throw new DomainException("La cantidad contada no puede ser negativa");
        }
        this.countedQuantity = countedQuantity;
    }

    void assertCounted() {
        if (countedQuantity == null) {
            throw new DomainException("Hay líneas sin cantidad contada");
        }
    }

    public int adjustmentDelta() {
        assertCounted();
        return countedQuantity - systemQuantity;
    }

    public Long getId() {
        return id;
    }

    public CycleCount getCycleCount() {
        return cycleCount;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getInventoryItemId() {
        return inventoryItemId;
    }

    public Integer getSystemQuantity() {
        return systemQuantity;
    }

    public Integer getCountedQuantity() {
        return countedQuantity;
    }
}
