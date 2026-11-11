package com.valeerp.catalog;

import jakarta.validation.constraints.NotBlank;

public final class BrandDtos {

    private BrandDtos() {
    }

    public record BrandResponse(Long id, String name, String code, String website, String logoUrl) {
    }

    public record CreateBrandRequest(
            @NotBlank String name,
            @NotBlank String code,
            String website,
            String logoUrl
    ) {
    }

    public record UpdateBrandRequest(
            @NotBlank String name,
            @NotBlank String code,
            String website,
            String logoUrl
    ) {
    }
}
