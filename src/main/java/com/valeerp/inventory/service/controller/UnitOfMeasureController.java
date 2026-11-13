package com.valeerp.inventory.service.controller;

import com.valeerp.catalog.CatalogUomDtos.CreateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UpdateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UomResponse;
import com.valeerp.catalog.CatalogUomService;
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
 * Fachada legacy. Preferir {@code /api/catalog/units-of-measure}.
 */
@RestController
@RequestMapping("/api/inventory/units-of-measure")
public class UnitOfMeasureController {

    private final CatalogUomService catalogUomService;

    public UnitOfMeasureController(CatalogUomService catalogUomService) {
        this.catalogUomService = catalogUomService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public List<UomResponse> getAllUnits() {
        return catalogUomService.listUnits();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UomResponse createUnit(@Valid @RequestBody CreateUomRequest request) {
        return catalogUomService.createUnit(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UomResponse updateUnit(@PathVariable Long id, @Valid @RequestBody UpdateUomRequest request) {
        return catalogUomService.updateUnit(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteUnit(@PathVariable Long id) {
        catalogUomService.retireUnit(id);
    }
}
