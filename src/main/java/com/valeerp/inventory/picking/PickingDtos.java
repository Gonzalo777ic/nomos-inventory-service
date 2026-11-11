package com.valeerp.inventory.picking;

import com.nomos.inventory.service.model.PickingStatus;
import com.valeerp.inventory.OperationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class PickingDtos {

    private PickingDtos() {
    }

    public record PickingLineRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity,
            Long inventoryItemId,
            String lotNumber,
            String serialNumber,
            Long sourceLocationId,
            Long destinationLocationId
    ) {
    }

    public record CreatePickingRequest(
            @NotNull Long companyId,
            @NotBlank String code,
            @NotNull OperationType operationType,
            @NotNull Long sourceWarehouseId,
            Long destinationWarehouseId,
            String notes,
            @NotEmpty @Valid List<PickingLineRequest> lines
    ) {
    }

    public record AddPickingLineRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity,
            Long inventoryItemId,
            String lotNumber,
            String serialNumber,
            Long sourceLocationId,
            Long destinationLocationId
    ) {
    }

    public record PickingLineResponse(
            Long id,
            Long productId,
            Integer quantity,
            Long inventoryItemId,
            String lotNumber,
            String serialNumber,
            Long sourceLocationId,
            Long destinationLocationId,
            boolean reserved
    ) {
    }

    public record PickingResponse(
            Long id,
            Long companyId,
            String code,
            OperationType operationType,
            PickingStatus status,
            Long sourceWarehouseId,
            Long destinationWarehouseId,
            String notes,
            Instant createdAt,
            Instant confirmedAt,
            Instant completedAt,
            Instant cancelledAt,
            String createdBy,
            List<PickingLineResponse> lines
    ) {
    }
}
