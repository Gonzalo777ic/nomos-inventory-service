package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 🎯 Importar
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Data
// 🎯 CORRECCIÓN CLAVE: Jackson ignora metadatos de Hibernate y la referencia recursiva.
// El 'product' se ignora aquí por seguridad si la carga perezosa persiste,
// o se podría quitar 'product' si se cambia la estrategia de Fetch.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🎯 CORRECCIÓN: Cambiado a EAGER Fetch.
    // Esto fuerza a que el Product se cargue junto con el InventoryItem en una sola consulta.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    // Cantidad actual disponible para esta existencia/lote.
    @Column(nullable = false)
    private Integer currentStock;

    // Costo unitario de adquisición para este lote específico.
    private Double unitCost;

    // Atributos de inventario físico
    private String lotNumber;
    private LocalDate expirationDate; // Fecha de Vencimiento
    private String location; // Ubicación en el almacén (ej: 'Aisle 3, Shelf B')

    // Campos de auditoría simple
    private LocalDateTime entryDate = LocalDateTime.now();
}
