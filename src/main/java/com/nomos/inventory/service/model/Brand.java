package com.nomos.inventory.service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

// Usaremos Lombok para simplificar el código
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "brands")
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    // UniqueConstraint será manejado por el DDL generado o la base de datos.
    private String name;

    // Código o abreviatura (Opcional, puede ser null)
    private String code;

    private String website;

    // URL del logo para mostrar en el frontend
    private String logoUrl;

    // NOTA: Para una validación estricta de 'code' y 'name' como UNIQUE,
    // es mejor usar @Table(uniqueConstraints = {...}) si usas DDL
}