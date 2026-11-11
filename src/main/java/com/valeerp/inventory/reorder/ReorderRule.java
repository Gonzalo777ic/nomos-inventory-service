package com.valeerp.inventory.reorder;

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
 * Regla de reabastecimiento min/max por producto y almacén. Maestro con soft-delete.
 */
@Entity
@Table(name = "reorder_rules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reorder_product_warehouse", columnNames = {"product_id", "warehouse_id"})
})
public class ReorderRule extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(name = "max_quantity", nullable = false)
    private Integer maxQuantity;

    protected ReorderRule() {
    }

    public static ReorderRule define(Long productId, Long warehouseId, int minQuantity, int maxQuantity) {
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        if (warehouseId == null) {
            throw new DomainException("warehouseId es obligatorio");
        }
        StockReorderPolicy.assertValidBounds(minQuantity, maxQuantity);
        ReorderRule rule = new ReorderRule();
        rule.productId = productId;
        rule.warehouseId = warehouseId;
        rule.minQuantity = minQuantity;
        rule.maxQuantity = maxQuantity;
        return rule;
    }

    public void redefine(int minQuantity, int maxQuantity) {
        StockReorderPolicy.assertValidBounds(minQuantity, maxQuantity);
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public boolean isBelowMinimum(int onHand) {
        return onHand < minQuantity;
    }

    public int suggestedReplenishment(int onHand) {
        return StockReorderPolicy.suggestedReplenishment(onHand, minQuantity, maxQuantity);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }
}
