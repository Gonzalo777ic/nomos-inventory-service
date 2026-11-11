package com.valeerp.catalog;

import com.valeerp.catalog.CatalogProductDtos.CreateProductRequest;
import com.valeerp.catalog.CatalogProductDtos.ProductResponse;
import com.valeerp.catalog.CatalogProductDtos.UpdateProductRequest;
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

/**
 * CRUD de maestros de producto en Catálogo. Sin endpoints de cantidad/stock.
 */
@RestController
@RequestMapping("/api/catalog/product-masters")
public class CatalogProductController {

    private final CatalogProductService catalogProductService;

    public CatalogProductController(CatalogProductService catalogProductService) {
        this.catalogProductService = catalogProductService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public List<ProductResponse> list() {
        return catalogProductService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ProductResponse get(@PathVariable Long id) {
        return catalogProductService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return catalogProductService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateProductRequest request) {
        return catalogProductService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        catalogProductService.retire(id);
    }
}
