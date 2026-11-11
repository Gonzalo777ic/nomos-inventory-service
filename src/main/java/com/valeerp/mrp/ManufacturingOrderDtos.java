package com.valeerp.mrp;

import com.valeerp.kernel.Money;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class ManufacturingOrderDtos {

    private ManufacturingOrderDtos() {
    }

    public record CreateRequest(
            @NotBlank String code,
            @NotNull Long bomId,
            @NotNull Long warehouseId,
            @Min(1) int quantityToProduce,
            Money unitCost,
            String notes
    ) {
    }

    public record ComponentLineResponse(
            Long id,
            Long productId,
            int plannedQuantity,
            int consumedQuantity
    ) {
    }

    public record Response(
            Long id,
            String code,
            Long bomId,
            Long productId,
            Long warehouseId,
            int quantityToProduce,
            int quantityProduced,
            ManufacturingOrderStatus status,
            Money unitCost,
            String notes,
            Instant confirmedAt,
            Instant startedAt,
            Instant completedAt,
            List<ComponentLineResponse> components
    ) {
    }
}
