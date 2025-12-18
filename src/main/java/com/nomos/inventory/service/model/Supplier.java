package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "La identificación fiscal (taxId) es obligatoria")
    private String taxId;

    @Email(message = "El formato del email no es válido")
    private String email;

    private String phone;

    private String address;

    @Column(length = 100)
    private String contactName;
}
