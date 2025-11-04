package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad Maestra: Define un tipo de atributo variable (Ej: Color, Talla, Sabor).
 * Este atributo luego se vincula a un Producto con un valor específico.
 */
@Entity
@Table(name = "product_attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del atributo (Ej: "Color", "Talla", "Material"). Debe ser único.
    @NotBlank(message = "El nombre del atributo es obligatorio")
    @Column(unique = true, nullable = false)
    private String name;

    // Tipo de dato esperado para el valor (Ej: "STRING", "NUMBER", "BOOLEAN", etc.)
    @NotBlank(message = "El tipo de dato es obligatorio")
    private String dataType;

    // TODO: Considerar añadir una restricción de lista de valores permitidos (Enum o JSON/String)
}
