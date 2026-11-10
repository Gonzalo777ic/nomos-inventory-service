package com.valeerp.catalog;

import com.valeerp.catalog.CatalogQueryService.CatalogProduct;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/api/catalog/products")
    public Page<CatalogProduct> search(@RequestParam(defaultValue = "") String search,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "12") int size) {
        return catalogQueryService.search(search, page, size);
    }

    @GetMapping({"/api/catalog/products/{id}", "/api/integration/catalog/products/{id}"})
    public CatalogProduct get(@PathVariable Long id) {
        return catalogQueryService.get(id);
    }
}
