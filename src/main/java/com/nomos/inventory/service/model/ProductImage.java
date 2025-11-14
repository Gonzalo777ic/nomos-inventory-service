package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // URL/Path de la imagen
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // Indica si es la imagen principal para el frontend
    @NotNull
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    // Orden en la galería
    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    // Mapeo bidireccional (opcional, útil para la navegación, pero no crea una columna)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    @JsonIgnore
    private Product product;
}