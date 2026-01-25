package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "lotNumber"})
})
@Data // Lombok generará getQuantity() y setQuantity() automáticamente
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryItem {

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

    // --- CAMBIO CLAVE ---
    // Renombrado de 'currentStock' a 'quantity' para que Lombok genere
    // getQuantity() y setQuantity(), solucionando el error en tu Controller.
    @Column(nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
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

    // Usamos @PrePersist para asegurar que la fecha se asigne antes de guardar
    // Esto es más seguro que inicializarla en la declaración del campo.
    @PrePersist
    protected void onCreate() {
        if (this.entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
    }
}