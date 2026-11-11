package com.valeerp.inventory.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class InternalLocationDtos {

    private InternalLocationDtos() {
    }

    public record InternalLocationResponse(
            Long id,
            Long warehouseId,
            String code,
            String name,
            InternalLocationType locationType,
            boolean defaultForType
    ) {
    }

    public record CreateInternalLocationRequest(
            @NotNull Long warehouseId,
            @NotBlank String code,
            @NotBlank String name,
            @NotNull InternalLocationType locationType,
            Boolean defaultForType
    ) {
    }

    public record UpdateInternalLocationRequest(
            @NotBlank String code,
            @NotBlank String name,
            Boolean defaultForType
    ) {
    }
}
