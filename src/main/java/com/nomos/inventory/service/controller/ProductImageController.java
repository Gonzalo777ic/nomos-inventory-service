package com.nomos.inventory.service.controller;

import com.valeerp.catalog.ProductMediaDtos.AttachFileRequest;
import com.valeerp.catalog.ProductMediaDtos.AttachImageRequest;
import com.valeerp.catalog.ProductMediaDtos.AttachmentResponse;
import com.valeerp.catalog.ProductMediaDtos.ImageResponse;
import com.valeerp.catalog.ProductMediaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Metadatos de imagen/adjunto. No sube bytes a storage externo: solo persiste path/URL/MIME/size.
 */
@RestController
@RequestMapping("/api/inventory/product-images")
public class ProductImageController {

    private final ProductMediaService productMediaService;

    public ProductImageController(ProductMediaService productMediaService) {
        this.productMediaService = productMediaService;
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public List<ImageResponse> getImagesByProduct(@PathVariable Long productId) {
        return productMediaService.listImages(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ImageResponse addProductImage(@Valid @RequestBody AttachImageRequest request) {
        return productMediaService.attachImage(request);
    }

    @PostMapping("/add-url")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ImageResponse addProductImageFromUrl(@Valid @RequestBody AttachImageRequest request) {
        return productMediaService.attachImage(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireProductImage(@PathVariable Long id) {
        productMediaService.retireImage(id);
    }

    @GetMapping("/product/{productId}/attachments")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public List<AttachmentResponse> listAttachments(@PathVariable Long productId) {
        return productMediaService.listAttachments(productId);
    }

    @PostMapping("/attachments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AttachmentResponse attachFile(@Valid @RequestBody AttachFileRequest request) {
        return productMediaService.attachFile(request);
    }

    @DeleteMapping("/attachments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireAttachment(@PathVariable Long id) {
        productMediaService.retireAttachment(id);
    }
}
