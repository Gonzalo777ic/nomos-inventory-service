package com.valeerp.catalog;

import java.math.BigDecimal;

public record ProductView(
        Long id,
        String sku,
        String name,
        Long brandId,
        BigDecimal price,
        Integer minStockThreshold,
        Long categoryId,
        Long unitOfMeasureId,
        String imageUrl,
        String brandName,
        String categoryName,
        String unitOfMeasureName,
        String supplierName
) {
}
