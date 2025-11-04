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
        // Asegura que no haya dos lotes con el mismo número para el mismo producto en el mismo almacén
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

    // 1. FK a Product
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "El producto es obligatorio")
    private Product product;

    // 2. FK a Warehouse
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @NotNull(message = "El almacén es obligatorio")
    private Warehouse warehouse;

    // Cantidad actual disponible. Debe ser >= 0.
    @Column(nullable = false)
    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer currentStock;

    // Costo unitario de adquisición para este lote.
    @NotNull(message = "El costo unitario es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser negativo")
    private Double unitCost;

    // Número de Lote (parte de la clave de unicidad)
    @NotBlank(message = "El número de lote es obligatorio")
    @Column(nullable = false)
    private String lotNumber;

    // Atributos de inventario físico
    private LocalDate expirationDate; // Fecha de Vencimiento

    // Ubicación física dentro del almacén (ej: 'Pasillo A, Estante 3')
    private String location;

    // Fecha en que este lote fue registrado en el sistema
    @Column(nullable = false, updatable = false)
    private LocalDateTime entryDate = LocalDateTime.now();
}
