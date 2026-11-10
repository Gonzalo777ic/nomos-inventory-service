package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "lotNumber"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryItem extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @NotNull(message = "El almacén es obligatorio")
    private Warehouse warehouse;



    @Column(nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Setter(AccessLevel.NONE)
    private Integer quantity;

    @NotNull(message = "El costo unitario es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private Double unitCost;

    @NotBlank(message = "El número de lote es obligatorio")
    @Column(nullable = false)
    private String lotNumber;

    private LocalDate expirationDate;

    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime entryDate;


    @PrePersist
    protected void onCreate() {
        if (this.entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
        if (this.quantity == null) {
            this.quantity = 0;
        }
    }

    public static InventoryItem open(Product product, Warehouse warehouse, String lotNumber, java.math.BigDecimal unitCost) {
        InventoryItem item = new InventoryItem();
        item.product = product;
        item.warehouse = warehouse;
        item.lotNumber = lotNumber;
        item.unitCost = unitCost == null ? 0.0 : unitCost.doubleValue();
        item.quantity = 0;
        item.entryDate = LocalDateTime.now();
        return item;
    }

    public void applyDelta(int delta) {
        int current = this.quantity == null ? 0 : this.quantity;
        int next = current + delta;
        if (next < 0) {
            throw new DomainException("Stock insuficiente en el lote " + lotNumber);
        }
        this.quantity = next;
    }
}