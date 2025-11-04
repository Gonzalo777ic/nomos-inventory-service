package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entidad de Catálogo: Clasificación jerárquica de productos.
 * Utiliza recursividad (parent) para crear subcategorías.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
// Ignora campos internos de JPA/Hibernate para serialización a JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la categoría (Ej: Laptops, Tablets, Accesorios)
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    // Descripción detallada de la categoría
    private String description;

    // Relación recursiva ManyToOne para la categoría padre.
    // fetch = FetchType.LAZY es crucial para evitar bucles de carga infinita.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Opcional: Puede ser útil en el frontend para mostrar el ID de la categoría padre.
    // Aunque el objeto 'parent' ya lo maneja, a veces es práctico para DTOs.
    // private Long parentId;
}
