package com.nomos.inventory.service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad maestra que define un Producto vendible o inventariable.
 * Contiene todas las claves foráneas a las tablas de apoyo (Maestros).
 * La propiedad imageUrl ha sido re-agregada.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código de Identificación Único del Producto (Stock Keeping Unit)
    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    // CLAVE FORÁNEA a Brand (Marca / Fabricante)
    @NotNull
    private Long brandId;

    // Precio Base del Producto
    @NotNull
    @Min(0)
    private Double price;

    // URL de la imagen del producto (re-agregado)
    private String imageUrl;

    // Nivel de stock mínimo para generar una alerta de reposición
    @NotNull
    @Min(0)
    private Integer minStockThreshold;

    // CLAVE FORÁNEA a Category (Clasificación jerárquica)
    @NotNull
    private Long categoryId;

    // CLAVE FORÁNEA a Supplier (Proveedor preferido para la compra)
    @NotNull
    private Long defaultSupplierId;

    // CLAVE FORÁNEA a UnitOfMeasure (Unidad de medida de inventario/venta)
    @NotNull
    private Long unitOfMeasureId;
}
