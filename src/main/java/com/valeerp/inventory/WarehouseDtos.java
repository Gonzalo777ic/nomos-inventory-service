package com.valeerp.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class WarehouseDtos {

    private WarehouseDtos() {
    }

    public record WarehouseResponse(
            Long id,
            Long companyId,
            Long branchId,
            String code,
            String name,
            String locationAddress,
            boolean main
    ) {
    }

    public record CreateWarehouseRequest(
            @NotNull Long companyId,
            Long branchId,
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String locationAddress,
            Boolean main
    ) {
    }

    public record UpdateWarehouseRequest(
            Long branchId,
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String locationAddress,
            Boolean main
    ) {
    }
}
