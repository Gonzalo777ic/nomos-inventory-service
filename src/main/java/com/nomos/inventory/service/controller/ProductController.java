package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.valeerp.catalog.CatalogMoneySupport;
import com.valeerp.catalog.CatalogProductDtos.CreateProductRequest;
import com.valeerp.catalog.CatalogProductDtos.ProductResponse;
import com.valeerp.catalog.CatalogProductDtos.UpdateProductRequest;
import com.valeerp.catalog.CatalogProductService;
import com.valeerp.catalog.ProductCatalogDtos.ApplyCostRequest;
import com.valeerp.kernel.Money;
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
@RequestMapping("/api/inventory/products")
public class ProductController {

    private final CatalogProductService catalogProductService;
    private final InventoryItemRepository inventoryItemRepository;

    public ProductController(CatalogProductService catalogProductService,
                             InventoryItemRepository inventoryItemRepository) {
        this.catalogProductService = catalogProductService;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public List<ProductResponse> getAllProducts() {
        return catalogProductService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ProductResponse getProductById(@PathVariable Long id) {
        return catalogProductService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return catalogProductService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ProductResponse updateProduct(@PathVariable Long id,
                                         @Valid @RequestBody UpdateProductRequest request) {
        return catalogProductService.update(id, request);
    }

    @PostMapping("/{id}/cost/incoming")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ProductResponse applyIncomingCost(@PathVariable Long id,
                                             @Valid @RequestBody ApplyCostRequest request) {
        Money unitCost = CatalogMoneySupport.ofAmount(request.unitCostAmount(), request.unitCostCurrency());
        return catalogProductService.applyIncomingCost(
                id, unitCost, request.onHandQtyBefore(), request.incomingQty());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireProduct(@PathVariable Long id) {
        catalogProductService.retire(id);
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public Integer getProductTotalStock(@PathVariable Long id) {
        catalogProductService.get(id);
        Integer totalStock = inventoryItemRepository.calculateTotalStockByProductId(id);
        return totalStock != null ? totalStock : 0;
    }
}
