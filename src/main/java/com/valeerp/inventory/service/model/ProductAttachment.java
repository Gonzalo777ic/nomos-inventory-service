package com.valeerp.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Adjunto de producto como metadatos locales (sin storage externo obligatorio).
 */
@Entity
@Table(name = "product_attachments")
public class ProductAttachment extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "file_name", length = 255)
    private String fileName;

    protected ProductAttachment() {
    }

    private ProductAttachment(Long productId, Long variantId, String label, String url,
                              String storagePath, String contentType, Long sizeBytes, String fileName) {
        this.productId = productId;
        this.variantId = variantId;
        this.label = label;
        this.url = url;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
    }

    public static ProductAttachment attach(Long productId, Long variantId, String label, String url,
                                           String storagePath, String contentType, Long sizeBytes,
                                           String fileName) {
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        if (label == null || label.isBlank()) {
            throw new DomainException("La etiqueta del adjunto es obligatoria");
        }
        if (url == null || url.isBlank()) {
            throw new DomainException("La URL del adjunto es obligatoria");
        }
        if (sizeBytes != null && sizeBytes < 0) {
            throw new DomainException("El tamaño no puede ser negativo");
        }
        return new ProductAttachment(
                productId,
                variantId,
                label.trim(),
                url.trim(),
                blankToNull(storagePath),
                blankToNull(contentType),
                sizeBytes,
                blankToNull(fileName)
        );
    }

    public void relabel(String label) {
        if (label == null || label.isBlank()) {
            throw new DomainException("La etiqueta del adjunto es obligatoria");
        }
        this.label = label.trim();
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

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
