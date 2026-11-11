package com.valeerp.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProductMediaDtos {

    private ProductMediaDtos() {
    }

    public record ImageResponse(
            Long id,
            Long productId,
            Long variantId,
            String imageUrl,
            String storagePath,
            String contentType,
            Long sizeBytes,
            String fileName,
            boolean main,
            int sortOrder
    ) {
    }

    public record AttachImageRequest(
            @NotNull Long productId,
            Long variantId,
            @NotBlank String imageUrl,
            String storagePath,
            String contentType,
            Long sizeBytes,
            String fileName,
            Boolean main,
            Integer sortOrder
    ) {
    }

    public record AttachmentResponse(
            Long id,
            Long productId,
            Long variantId,
            String label,
            String url,
            String storagePath,
            String contentType,
            Long sizeBytes,
            String fileName
    ) {
    }

    public record AttachFileRequest(
            @NotNull Long productId,
            Long variantId,
            @NotBlank String label,
            @NotBlank String url,
            String storagePath,
            String contentType,
            Long sizeBytes,
            String fileName
    ) {
    }
}
