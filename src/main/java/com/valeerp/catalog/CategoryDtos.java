package com.valeerp.catalog;

import jakarta.validation.constraints.NotBlank;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String description,
            Long parentId,
            boolean root
    ) {
    }

    public record CreateCategoryRequest(
            @NotBlank String name,
            String description,
            Long parentId
    ) {
    }

    public record UpdateCategoryRequest(
            @NotBlank String name,
            String description,
            Long parentId
    ) {
    }
}
