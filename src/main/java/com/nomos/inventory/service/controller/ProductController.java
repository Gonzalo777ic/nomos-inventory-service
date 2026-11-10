package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.ProductSupplier;
import com.nomos.inventory.service.repository.BrandRepository;
import com.nomos.inventory.service.repository.CategoryRepository;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductImageRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.ProductSupplierRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import com.nomos.inventory.service.repository.UnitOfMeasureRepository;
import com.valeerp.catalog.ProductView;
import com.valeerp.kernel.Actors;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final SupplierRepository supplierRepository;
    private final ProductImageRepository productImageRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductView>> getAllProducts() {
        List<ProductView> products = productRepository.findAll().stream().map(this::toView).toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<ProductView> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(toView(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El SKU '" + product.getSku() + "' ya está registrado en otro producto.");
        }
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toView(savedProduct));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        return productRepository.findById(id).map(product -> {
            if (productRepository.existsBySkuAndIdNot(productDetails.getSku(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El SKU '" + productDetails.getSku() + "' ya está registrado en otro producto.");
            }
            product.setSku(productDetails.getSku());
            product.setName(productDetails.getName());
            product.setBrandId(productDetails.getBrandId());
            product.changePrice(productDetails.getPrice());
            product.setMinStockThreshold(productDetails.getMinStockThreshold());
            product.setCategoryId(productDetails.getCategoryId());
            product.setUnitOfMeasureId(productDetails.getUnitOfMeasureId());
            return ResponseEntity.ok(toView(productRepository.save(product)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> retireProduct(@PathVariable Long id) {
        return productRepository.findById(id).map(product -> {
            product.retire(Actors.current(), Instant.now());
            productRepository.save(product);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<Integer> getProductTotalStock(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Integer totalStock = inventoryItemRepository.calculateTotalStockByProductId(id);
        return ResponseEntity.ok(totalStock != null ? totalStock : 0);
    }

    private ProductView toView(Product product) {
        String brandName = brandRepository.findById(product.getBrandId()).map(brand -> brand.getName()).orElse(null);
        String categoryName = categoryRepository.findById(product.getCategoryId()).map(category -> category.getName()).orElse(null);
        String unitName = unitOfMeasureRepository.findById(product.getUnitOfMeasureId()).map(unit -> unit.getName()).orElse(null);
        String supplierName = productSupplierRepository.findByProductIdAndIsPreferred(product.getId(), true).stream()
                .findFirst()
                .map(ProductSupplier::getSupplierId)
                .flatMap(supplierRepository::findById)
                .map(supplier -> supplier.getName())
                .orElse(null);
        String imageUrl = productImageRepository.findByProductIdAndIsMain(product.getId(), true)
                .or(() -> productImageRepository.findTopByProductIdOrderBySortOrderAsc(product.getId()))
                .map(image -> image.getImageUrl())
                .orElse(null);
        return new ProductView(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getBrandId(),
                product.getPrice(),
                product.getMinStockThreshold(),
                product.getCategoryId(),
                product.getUnitOfMeasureId(),
                imageUrl,
                brandName,
                categoryName,
                unitName,
                supplierName
        );
    }
}
