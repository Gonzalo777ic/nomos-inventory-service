package com.valeerp.mrp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class BomDtos {

    private BomDtos() {
    }

    public record LineRequest(
            @NotNull Long componentProductId,
            @Min(1) int quantityPer,
            Long childBomId,
            String notes
    ) {
    }

    public record CreateRequest(
            @NotBlank String code,
            @NotNull Long productId,
            @Min(1) Integer outputQuantity,
            String notes,
            @NotEmpty @Valid List<LineRequest> lines
    ) {
    }

    public record LineResponse(
            Long id,
            Long componentProductId,
            int quantityPer,
            Long childBomId,
            String notes
    ) {
    }

    public record Response(
            Long id,
            String code,
            Long productId,
            int outputQuantity,
            BomStatus status,
            String notes,
            List<LineResponse> lines
    ) {
    }

    public record ExplosionRequest(
            @Min(1) int finishedQuantity
    ) {
    }

    public record ExplosionLineResponse(
            Long componentProductId,
            int quantity,
            int depth,
            boolean leaf
    ) {
    }

    public record ExplosionResponse(
            Long bomId,
            String bomCode,
            Long productId,
            int finishedQuantity,
            List<ExplosionLineResponse> components
    ) {
    }
}
