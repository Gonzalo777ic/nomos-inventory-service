package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que registra la trazabilidad histórica de los cambios de stock.
 */
@Entity
@Data
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK a InventoryItem (asumiendo que existe una entidad InventoryItem para stock detallado)
    // Permite trazar qué registro de stock específico fue afectado.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem; // Asumiendo InventoryItem existe

    // FK a Product (para referencia general del producto movido)
    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "El cambio de cantidad es obligatorio")
    private Integer quantityChange; // Puede ser positivo (entrada) o negativo (salida)

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    private MovementType type; // ENTRADA, SALIDA, TRASPASO

    @NotNull(message = "La razón del movimiento es obligatoria")
    private String reason;

    @NotNull(message = "La fecha y hora del movimiento es obligatoria")
    @PastOrPresent(message = "La fecha de movimiento no puede ser futura")
    private LocalDateTime movementDate;

    // Campos de referencia para trazar el origen del movimiento (Ej: Orden de Compra, Factura de Venta)
    private Long referenceId;

    private String referenceService; // Ej: "PurchaseOrder", "SalesInvoice", "StockAdjustment"

    // Constructor sin argumentos requerido por JPA
    public InventoryMovement() {}
}
