package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(nullable = false)
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotNull(message = "La marca es obligatoria")
    @Column(name = "brand", nullable = false)
    private Long brandId;

    @NotNull(message = "El precio es obligatorio")
    @Setter(lombok.AccessLevel.NONE)
    private BigDecimal price;

    @NotNull(message = "El umbral de stock mínimo es obligatorio")
    @Min(value = 0, message = "El umbral de stock mínimo no puede ser negativo")
    private Integer minStockThreshold;

    @NotNull(message = "La categoría es obligatoria")
    @Column(name = "category", nullable = false)
    private Long categoryId;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Column(name = "unit_of_measure", nullable = false)
    private Long unitOfMeasureId;

    public void changePrice(BigDecimal nextPrice) {
        if (nextPrice == null || nextPrice.signum() < 0) {
            throw new DomainException("El precio no puede ser negativo");
        }
        this.price = nextPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public void setPrice(BigDecimal nextPrice) {
        changePrice(nextPrice);
    }
}
