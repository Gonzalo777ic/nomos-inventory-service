package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // Importación necesaria
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList; // Importación para inicializar la lista
import java.util.List;

/**
 * Entidad que representa la Orden de Compra (Pedido) a un proveedor.
 */
@Entity
@Data
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK a Supplier (Debe ser LAZY, se cargará con Fetch Join o @Transactional)
    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier; // Aquí no ponemos @JsonIgnore, ya que es ManyToOne y no causa bucle, pero podría causar LIE.

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

    // Relación de Detalles (OneToMany). Es LAZY por defecto y debe ser ignorada en la serialización
    // general para evitar bucles y LazyInitializationException en el GET /all.
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // ⭐ CORRECCIÓN: Permite la lectura y ESCRITURA (POST/PUT) del array 'details'.
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    // Constructor sin argumentos requerido por JPA
    public PurchaseOrder() {
        // Inicializa el estado por defecto
        this.status = OrderStatus.PENDIENTE;
        this.totalAmount = 0.0;
    }
}