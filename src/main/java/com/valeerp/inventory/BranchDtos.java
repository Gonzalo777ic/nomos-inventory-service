package com.valeerp.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class BranchDtos {

    private BranchDtos() {
    }

    public record BranchResponse(
            Long id,
            Long companyId,
            String code,
            String name,
            String address
    ) {
    }

    public record CreateBranchRequest(
            @NotNull Long companyId,
            @NotBlank String code,
            @NotBlank String name,
            String address
    ) {
    }

    public record UpdateBranchRequest(
            @NotBlank String code,
            @NotBlank String name,
            String address
    ) {
    }
}
