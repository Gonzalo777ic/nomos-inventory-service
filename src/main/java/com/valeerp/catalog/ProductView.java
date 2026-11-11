package com.valeerp.catalog;

import java.math.BigDecimal;

/**
 * Vista legada de catálogo. Preferir {@link CatalogProductDtos.ProductResponse}.
 */
public record ProductView(
        Long id,
        String sku,
        String name,
        Long brandId,
        BigDecimal price,
        String priceCurrency,
        String barcode,
        Integer minStockThreshold,
        Long categoryId,
        Long unitOfMeasureId,
        CostingMethod costingMethod,
        BigDecimal standardCost,
        BigDecimal averageCost,
        String imageUrl,
        String brandName,
        String categoryName,
        String unitOfMeasureName,
        String supplierName
) {
}
