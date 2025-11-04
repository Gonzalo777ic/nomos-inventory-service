package com.nomos.inventory.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad maestra para representar las Marcas de los Productos.
 * Incluye campos para nombre, código único, sitio web y URL del logo.
 */
@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre completo de la marca (Ej: "Hewlett-Packard")
    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Column(unique = true, nullable = false)
    @Size(max = 100)
    private String name;

    // Código o abreviatura de la marca (Ej: "HP", "SAM"). Debe ser único.
    @NotBlank(message = "El código de la marca es obligatorio")
    @Column(unique = true, nullable = false, length = 20)
    @Size(max = 20)
    private String code;

    // URL del sitio web oficial de la marca (opcional)
    @Size(max = 255)
    private String website;

    // URL del logo de la marca para mostrar en el frontend (opcional)
    @Size(max = 255)
    private String logoUrl;
}
