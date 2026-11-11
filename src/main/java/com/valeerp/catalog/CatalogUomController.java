package com.valeerp.catalog;

import com.valeerp.catalog.CatalogUomDtos.ConvertRequest;
import com.valeerp.catalog.CatalogUomDtos.ConvertResponse;
import com.valeerp.catalog.CatalogUomDtos.ConversionResponse;
import com.valeerp.catalog.CatalogUomDtos.CreateConversionRequest;
import com.valeerp.catalog.CatalogUomDtos.CreateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UpdateConversionRequest;
import com.valeerp.catalog.CatalogUomDtos.UpdateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogUomController {

    private final CatalogUomService catalogUomService;

    public CatalogUomController(CatalogUomService catalogUomService) {
        this.catalogUomService = catalogUomService;
    }

    @GetMapping("/api/catalog/units-of-measure")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public List<UomResponse> listUnits() {
        return catalogUomService.listUnits();
    }

    @GetMapping("/api/catalog/units-of-measure/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public UomResponse getUnit(@PathVariable Long id) {
        return catalogUomService.getUnit(id);
    }

    @PostMapping("/api/catalog/units-of-measure")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UomResponse createUnit(@Valid @RequestBody CreateUomRequest request) {
        return catalogUomService.createUnit(request);
    }

    @PutMapping("/api/catalog/units-of-measure/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UomResponse updateUnit(@PathVariable Long id, @Valid @RequestBody UpdateUomRequest request) {
        return catalogUomService.updateUnit(id, request);
    }

    @DeleteMapping("/api/catalog/units-of-measure/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireUnit(@PathVariable Long id) {
        catalogUomService.retireUnit(id);
    }

    @GetMapping("/api/catalog/uom-conversions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public List<ConversionResponse> listConversions() {
        return catalogUomService.listConversions();
    }

    @PostMapping("/api/catalog/uom-conversions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ConversionResponse createConversion(@Valid @RequestBody CreateConversionRequest request) {
        return catalogUomService.createConversion(request);
    }

    @PutMapping("/api/catalog/uom-conversions/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ConversionResponse updateConversion(@PathVariable Long id,
                                               @Valid @RequestBody UpdateConversionRequest request) {
        return catalogUomService.updateConversion(id, request);
    }

    @DeleteMapping("/api/catalog/uom-conversions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireConversion(@PathVariable Long id) {
        catalogUomService.retireConversion(id);
    }

    @PostMapping("/api/catalog/uom-conversions/convert")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ConvertResponse convert(@Valid @RequestBody ConvertRequest request) {
        return catalogUomService.convert(request);
    }
}
