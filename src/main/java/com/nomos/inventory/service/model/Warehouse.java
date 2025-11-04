package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad: Define un Almacén o Ubicación física de stock.
 * Permite gestionar inventario en múltiples ubicaciones.
 */
@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre descriptivo del almacén (Ej: "Almacén Principal", "Tienda Sur"). Debe ser único.
    @NotBlank(message = "El nombre del almacén es obligatorio")
    @Column(unique = true, nullable = false)
    private String name;

    // Dirección o ubicación física del almacén
    @NotBlank(message = "La dirección de la ubicación es obligatoria")
    private String locationAddress;

    // Indicador si este es el almacén principal. Solo debe haber uno marcado como true.
    @NotNull(message = "Debe indicarse si es el almacén principal")
    private Boolean isMain = false;
}
