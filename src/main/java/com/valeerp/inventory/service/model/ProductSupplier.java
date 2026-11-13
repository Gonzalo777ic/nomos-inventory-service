package com.valeerp.inventory.service.model;

import com.valeerp.kernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "product_suppliers",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"productId", "supplierId"})})
@IdClass(ProductSupplier.ProductSupplierId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplier {

    @Getter
    @Setter
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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", nullable = false, length = 3))
    })
    @NotNull
    private Money unitCost;

    @NotNull
    @Min(0)
    private Integer leadTimeDays;

    @NotNull
    private Boolean isPreferred = false;

    @NotNull
    private Boolean isActive = true;
}
