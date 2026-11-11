package com.valeerp.catalog;

import com.valeerp.catalog.CatalogProductDtos.CreateVariantRequest;
import com.valeerp.catalog.CatalogProductDtos.UpdateVariantRequest;
import com.valeerp.catalog.CatalogProductDtos.VariantResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogVariantController {

    private final CatalogProductService catalogProductService;

    public CatalogVariantController(CatalogProductService catalogProductService) {
        this.catalogProductService = catalogProductService;
    }

    @GetMapping("/products/{productId}/variants")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public List<VariantResponse> listByProduct(@PathVariable Long productId) {
        return catalogProductService.listVariants(productId);
    }

    @GetMapping("/variants/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public VariantResponse get(@PathVariable Long id) {
        return catalogProductService.getVariant(id);
    }

    @PostMapping("/products/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VariantResponse create(@PathVariable Long productId,
                                  @Valid @RequestBody CreateVariantRequest request) {
        return catalogProductService.createVariant(productId, request);
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public VariantResponse update(@PathVariable Long productId,
                                  @PathVariable Long variantId,
                                  @Valid @RequestBody UpdateVariantRequest request) {
        return catalogProductService.updateVariant(productId, variantId, request);
    }

    @DeleteMapping("/products/{productId}/variants/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long productId, @PathVariable Long variantId) {
        catalogProductService.retireVariant(productId, variantId);
    }
}
