package com.valeerp.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogProductDtos {

    private CatalogProductDtos() {
    }

    public record MoneyDto(BigDecimal amount, String currency) {
    }

    public record ProductResponse(
            Long id,
            String sku,
            String name,
            String productType,
            String barcode,
            Long brandId,
            Long categoryId,
            Long unitOfMeasureId,
            MoneyDto listPrice,
            Integer minStockThreshold,
            CostingMethod costingMethod,
            MoneyDto standardCost,
            MoneyDto averageCost,
            MoneyDto valuationCost
    ) {
    }

    public record CreateProductRequest(
            @NotBlank String sku,
            @NotBlank String name,
            @NotBlank String productType,
            String barcode,
            @NotNull Long brandId,
            @NotNull Long categoryId,
            @NotNull Long unitOfMeasureId,
            @NotNull BigDecimal listPriceAmount,
            String listPriceCurrency,
            Integer minStockThreshold,
            CostingMethod costingMethod,
            BigDecimal standardCostAmount,
            String standardCostCurrency
    ) {
    }

    public record UpdateProductRequest(
            @NotBlank String sku,
            @NotBlank String name,
            @NotBlank String productType,
            String barcode,
            @NotNull Long brandId,
            @NotNull Long categoryId,
            @NotNull Long unitOfMeasureId,
            @NotNull BigDecimal listPriceAmount,
            String listPriceCurrency,
            Integer minStockThreshold,
            CostingMethod costingMethod,
            BigDecimal standardCostAmount,
            String standardCostCurrency
    ) {
    }

    public record AttributeValueDto(
            @NotNull Long attributeId,
            @NotBlank String value
    ) {
    }

    public record VariantResponse(
            Long id,
            Long productId,
            String sku,
            String barcode,
            String label,
            List<AttributeValueDto> attributes
    ) {
    }

    public record CreateVariantRequest(
            @NotBlank String sku,
            String barcode,
            String label,
            @Valid List<AttributeValueDto> attributes
    ) {
    }

    public record UpdateVariantRequest(
            @NotBlank String sku,
            String barcode,
            String label,
            @Valid List<AttributeValueDto> attributes
    ) {
    }
}
