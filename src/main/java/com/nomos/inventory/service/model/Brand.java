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


@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la marca es obligatorio")
    @Column(unique = true, nullable = false)
    @Size(max = 100)
    private String name;

    @NotBlank(message = "El código de la marca es obligatorio")
    @Column(unique = true, nullable = false, length = 20)
    @Size(max = 20)
    private String code;

    @Size(max = 255)
    private String website;

    @Size(max = 255)
    private String logoUrl;
}
