package com.valeerp.catalog;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTOs compartidos de catálogo (coste y Money view para listas de precios).
 */
public final class ProductCatalogDtos {

    private ProductCatalogDtos() {
    }

    public record MoneyView(BigDecimal amount, String currency) {
    }

    public record ApplyCostRequest(
            @NotNull BigDecimal unitCostAmount,
            String unitCostCurrency,
            @NotNull Integer onHandQtyBefore,
            @NotNull Integer incomingQty
    ) {
    }
}
