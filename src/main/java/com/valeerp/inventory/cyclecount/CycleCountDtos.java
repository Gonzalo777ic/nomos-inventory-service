package com.valeerp.inventory.cyclecount;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class CycleCountDtos {

    private CycleCountDtos() {
    }

    public record CycleCountLineRequest(
            @NotNull Long productId,
            @NotNull Long inventoryItemId,
            @NotNull @Min(0) Integer systemQuantity
    ) {
    }

    public record CreateCycleCountRequest(
            @NotNull Long companyId,
            @NotBlank String code,
            @NotNull Long warehouseId,
            String notes,
            @NotEmpty @Valid List<CycleCountLineRequest> lines
    ) {
    }

    public record RecordCountRequest(
            @NotNull @Min(0) Integer countedQuantity
    ) {
    }

    public record CycleCountLineResponse(
            Long id,
            Long productId,
            Long inventoryItemId,
            Integer systemQuantity,
            Integer countedQuantity,
            Integer adjustmentDelta
    ) {
    }

    public record CycleCountResponse(
            Long id,
            Long companyId,
            String code,
            Long warehouseId,
            CycleCountStatus status,
            String notes,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            Instant cancelledAt,
            String createdBy,
            List<CycleCountLineResponse> lines
    ) {
    }
}
