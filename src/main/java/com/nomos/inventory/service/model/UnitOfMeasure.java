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
 * Entidad maestra para la Unidad de Medida (Ej: Unidad, Kilogramo, Paquete).
 * Es fundamental para la consistencia del inventario y las transacciones.
 */
@Entity
@Table(name = "unit_of_measures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre completo de la unidad (Ej: "Kilogramo")
    @NotBlank(message = "El nombre de la unidad de medida es obligatorio")
    @Size(max = 100)
    private String name;

    // Abreviatura, debe ser única (Ej: "Kg", "Un", "Pqt")
    @NotBlank(message = "La abreviatura de la unidad de medida es obligatoria")
    @Column(unique = true, length = 10)
    @Size(max = 10)
    private String abbreviation;
}
