package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entidad que representa la Orden de Compra (Pedido) a un proveedor.
 */
@Entity
@Data
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK a Supplier
    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull(message = "La fecha de la orden es obligatoria")
    @PastOrPresent(message = "La fecha de la orden no puede ser futura")
    private LocalDate orderDate;

    @NotNull(message = "La fecha de entrega esperada es obligatoria")
    private LocalDate deliveryDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto total no puede ser negativo")
    private Double totalAmount;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Constructor sin argumentos requerido por JPA
    public PurchaseOrder() {
        // Inicializa el estado por defecto
        this.status = OrderStatus.PENDIENTE;
        this.totalAmount = 0.0;
    }

    // Nota: Aunque el campo supplierId se especificó como Long en el requisito,
    // JPA recomienda usar la relación ManyToOne completa con la entidad Supplier
    // para asegurar la integridad referencial.
}
