package com.nomos.inventory.service.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Entidad que representa el detalle (ítem) de una Orden de Compra.
 * Cada detalle relaciona una Orden de Compra con un Producto específico,
 * indicando la cantidad y el costo unitario de compra.
 */
@Entity
@Data
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK a PurchaseOrder
    @NotNull(message = "El ID de la Orden de Compra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonBackReference // ⭐ CORRECCIÓN: Ignora esta referencia para evitar el bucle.
    private PurchaseOrder purchaseOrder;

    // FK a Product (No necesita JsonIgnore si Product no referencia de vuelta a Detail)
    @NotNull(message = "El ID del Producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo unitario debe ser mayor a cero")
    private Double unitCost;

    // Constructor sin argumentos requerido por JPA
    public PurchaseOrderDetail() {}
}
