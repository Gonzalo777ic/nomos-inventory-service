package com.nomos.inventory.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotNull(message = "La marca es obligatoria")
    @Column(name = "brand", nullable = false)
    private Long brandId;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    @NotNull(message = "El umbral de stock mínimo es obligatorio")
    @Min(value = 0, message = "El umbral de stock mínimo no puede ser negativo")
    private Integer minStockThreshold;

    @NotNull(message = "La categoría es obligatoria")
    @Column(name = "category", nullable = false)
    private Long categoryId;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Column(name = "unit_of_measure", nullable = false)
    private Long unitOfMeasureId;

    @Transient
    private String imageUrl;

    @Transient // Evita que se mapee a la columna de la DB
    private String brandName;

    @Transient // Evita que se mapee a la columna de la DB
    private String categoryName;

    @Transient // Evita que se mapee a la columna de la DB
    private String unitOfMeasureName;

    @Transient
    private String supplierName;
}