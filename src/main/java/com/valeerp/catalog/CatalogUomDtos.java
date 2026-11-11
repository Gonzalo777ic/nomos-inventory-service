package com.valeerp.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class CatalogUomDtos {

    private CatalogUomDtos() {
    }

    public record UomResponse(
            Long id,
            String name,
            String abbreviation,
            String category
    ) {
    }

    public record CreateUomRequest(
            @NotBlank String name,
            @NotBlank String abbreviation,
            @NotBlank String category
    ) {
    }

    public record UpdateUomRequest(
            @NotBlank String name,
            @NotBlank String abbreviation,
            @NotBlank String category
    ) {
    }

    public record ConversionResponse(
            Long id,
            Long fromUomId,
            Long toUomId,
            BigDecimal factor
    ) {
    }

    public record CreateConversionRequest(
            @NotNull Long fromUomId,
            @NotNull Long toUomId,
            @NotBlank String factor
    ) {
    }

    public record UpdateConversionRequest(
            @NotBlank String factor
    ) {
    }

    public record ConvertRequest(
            @NotNull Long fromUomId,
            @NotNull Long toUomId,
            @NotNull BigDecimal quantity
    ) {
    }

    public record ConvertResponse(
            Long fromUomId,
            Long toUomId,
            BigDecimal sourceQuantity,
            BigDecimal convertedQuantity
    ) {
    }
}
