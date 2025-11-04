package com.nomos.inventory.service.model;

import jakarta.persistence.Column;
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
 * Nota: Se eliminó 'defaultSupplierId' para migrar a la tabla M:N ProductSupplier.
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

    // Código de Identificación Único del Producto (Stock Keeping Unit). DEBE SER ÚNICO.
    @NotBlank(message = "El SKU es obligatorio")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    // CLAVE FORÁNEA a Brand (Marca / Fabricante)
    // FIX: Usamos @Column para mapear brandId a la columna 'brand' (sin _id) según el hint de la DB.
    @NotNull(message = "La marca es obligatoria")
    @Column(name = "brand", nullable = false)
    private Long brandId;

    // Precio Base de Venta al cliente
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    // URL de la imagen del producto
    private String imageUrl;

    // Nivel de stock mínimo para generar una alerta de reposición
    @NotNull(message = "El umbral de stock mínimo es obligatorio")
    @Min(value = 0, message = "El umbral de stock mínimo no puede ser negativo")
    private Integer minStockThreshold;

    // CLAVE FORÁNEA a Category (Clasificación jerárquica)
    // FIX: Usamos @Column para mapear categoryId a la columna 'category' (sin _id).
    @NotNull(message = "La categoría es obligatoria")
    @Column(name = "category", nullable = false)
    private Long categoryId;

    // CLAVE FORÁNEA a UnitOfMeasure (Unidad de medida de inventario/venta)
    // FIX: Usamos @Column para mapear unitOfMeasureId a la columna 'unit_of_measure' (sin _id).
    @NotNull(message = "La unidad de medida es obligatoria")
    @Column(name = "unit_of_measure", nullable = false)
    private Long unitOfMeasureId;
}
