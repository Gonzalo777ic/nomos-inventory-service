package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * Entidad de Relación M:N entre Product y Supplier.
 * Almacena atributos específicos del vínculo entre un Producto y un Proveedor.
 */
@Entity
@Table(name = "product_suppliers",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"productId", "supplierId"})})
@IdClass(ProductSupplier.ProductSupplierId.class) // Referencia a la clase interna estática
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplier {

    /**
     * Clase de ID Compuesto para ProductSupplier (productId, supplierId).
     * Debe ser public static para que sea accesible por JPA/Hibernate y el repositorio.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSupplierId implements Serializable {
        private Long productId;
        private Long supplierId;
    }

    @Id
    @NotNull
    private Long productId;

    @Id
    @NotNull
    private Long supplierId;

    private String supplierProductCode;

    @NotNull
    @Min(0)
    private Double unitCost;

    @NotNull
    @Min(0)
    private Integer leadTimeDays;

    @NotNull
    private Boolean isPreferred = false;

    @NotNull
    private Boolean isActive = true;
}
