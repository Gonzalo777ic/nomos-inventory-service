package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem; 

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "El cambio de cantidad es obligatorio")
    private Integer quantityChange;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    private MovementType type; 

    @NotNull(message = "La razón del movimiento es obligatoria")
    private String reason;

    @NotNull(message = "La fecha y hora del movimiento es obligatoria")
    @PastOrPresent(message = "La fecha de movimiento no puede ser futura")
    private LocalDateTime movementDate;

    private Long referenceId;

    private String referenceService; 

    public InventoryMovement() {}
}
