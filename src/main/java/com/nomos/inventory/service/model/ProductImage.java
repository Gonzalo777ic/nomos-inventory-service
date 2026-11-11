package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Metadatos locales de imagen de producto (path/URL/content-type/size).
 * No exige storage externo real; la baja es lógica (sin delete físico).
 */
@Entity
@Table(name = "product_images")
public class ProductImage extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "is_main", nullable = false)
    private boolean main;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected ProductImage() {
    }

    private ProductImage(Long productId, Long variantId, String imageUrl, String storagePath,
                         String contentType, Long sizeBytes, String fileName, boolean main, int sortOrder) {
        this.productId = productId;
        this.variantId = variantId;
        this.imageUrl = imageUrl;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
        this.main = main;
        this.sortOrder = sortOrder;
    }

    public static ProductImage attach(Long productId, Long variantId, String imageUrl, String storagePath,
                                      String contentType, Long sizeBytes, String fileName,
                                      boolean main, Integer sortOrder) {
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new DomainException("La URL o ruta pública de la imagen es obligatoria");
        }
        if (sizeBytes != null && sizeBytes < 0) {
            throw new DomainException("El tamaño no puede ser negativo");
        }
        int order = sortOrder == null ? 0 : sortOrder;
        return new ProductImage(
                productId,
                variantId,
                imageUrl.trim(),
                blankToNull(storagePath),
                blankToNull(contentType),
                sizeBytes,
                blankToNull(fileName),
                main,
                order
        );
    }

    public void markAsMain() {
        this.main = true;
    }

    public void clearMain() {
        this.main = false;
    }

    public void reorder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isMain() {
        return main;
    }

    /** Compatibilidad con código legado. */
    public Boolean getIsMain() {
        return main;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
