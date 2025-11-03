package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "suppliers")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Nombre de la empresa proveedora (Ej: Editorial Planeta)

    @Column(unique = true)
    private String taxId; // RUC, NIF o ID Fiscal (Clave para facturación)

    private String contactName; // Persona de contacto

    private String phone;

    @Column(unique = true)
    private String email;

    // Dirección física o administrativa
    private String address;

    // Relación: Un proveedor puede suministrar muchos productos, pero un producto puede
    // venir de varios proveedores (si no lo definimos a nivel de lote).
    // Si la referencia del proveedor va a nivel de Producto, se establecería aquí la relación @OneToMany
    // Pero es más limpio enlazarlo a la entidad Product o InventoryItem.
}
