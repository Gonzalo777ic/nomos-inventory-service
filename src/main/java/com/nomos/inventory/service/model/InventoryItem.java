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

/**
 * Entidad: Representa un lote específico de existencias físicas
 * de un producto en un almacén.
 */
@Entity
@Table(name = "inventory_items", uniqueConstraints = {

        @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "lotNumber"})
})
@Data
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

    @Column(nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer currentStock;

    @NotNull(message = "El costo unitario es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private Double unitCost;

    @NotBlank(message = "El número de lote es obligatorio")
    @Column(nullable = false)
    private String lotNumber;

    private LocalDate expirationDate; // Fecha de Vencimiento

    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime entryDate = LocalDateTime.now();
}
