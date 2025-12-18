package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;


@Entity
@Table(name = "product_attribute_values",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"productId", "attributeId"})}) 
@IdClass(ProductAttributeValue.ProductAttributeValueId.class) 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeValue {

    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAttributeValueId implements Serializable {
        private Long productId;
        private Long attributeId;
    }

    @Id
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @Id
    @NotNull(message = "El ID del atributo es obligatorio")
    private Long attributeId;

    @NotBlank(message = "El valor del atributo es obligatorio")
    private String value;
}
