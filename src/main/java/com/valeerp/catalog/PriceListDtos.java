package com.valeerp.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class PriceListDtos {

    private PriceListDtos() {
    }

    public record PriceListResponse(
            Long id,
            String code,
            String name,
            PriceListKind kind,
            String currencyCode
    ) {
    }

    public record CreatePriceListRequest(
            @NotBlank String code,
            @NotBlank String name,
            @NotNull PriceListKind kind,
            String currencyCode
    ) {
    }

    public record PriceListItemResponse(
            Long id,
            Long priceListId,
            Long productId,
            Long variantId,
            ProductCatalogDtos.MoneyView price
    ) {
    }

    public record UpsertPriceListItemRequest(
            @NotNull Long productId,
            Long variantId,
            @NotNull BigDecimal amount,
            String currency
    ) {
    }

    public record PriceRuleResponse(
            Long id,
            Long priceListId,
            String name,
            Long partnerId,
            Long categoryId,
            int priority,
            BigDecimal percentAdjustment,
            ProductCatalogDtos.MoneyView fixedPrice
    ) {
    }

    public record CreatePriceRuleRequest(
            @NotBlank String name,
            Long partnerId,
            Long categoryId,
            @NotNull Integer priority,
            BigDecimal percentAdjustment,
            BigDecimal fixedAmount,
            String fixedCurrency
    ) {
    }

    public record ResolvePriceRequest(
            @NotNull Long productId,
            Long variantId,
            Long partnerId,
            Long priceListId,
            PriceListKind kind
    ) {
    }

    public record ResolvedPriceResponse(
            Long productId,
            Long variantId,
            Long priceListId,
            Long matchedRuleId,
            ProductCatalogDtos.MoneyView price,
            String source
    ) {
    }
}
