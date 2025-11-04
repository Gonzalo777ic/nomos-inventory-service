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

    // Clave primaria compuesta 1: FK a Product
    @Id
    @NotNull
    private Long productId;

    // Clave primaria compuesta 2: FK a Supplier
    @Id
    @NotNull
    private Long supplierId;

    // Código del producto que usa el proveedor (opcional)
    private String supplierProductCode;

    // Costo unitario estándar de compra a este proveedor
    @NotNull
    @Min(0)
    private Double unitCost;

    // Tiempo de entrega estimado en días
    @NotNull
    @Min(0)
    private Integer leadTimeDays;

    // Indica si este es el proveedor preferido para este producto
    @NotNull
    private Boolean isPreferred = false;

    // Indica si la relación está activa (para manejo histórico)
    @NotNull
    private Boolean isActive = true;
}
