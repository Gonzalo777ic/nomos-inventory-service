package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    // 1. GET (READ ALL)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // 2. POST (CREATE) - Añadida validación de unicidad de SKU
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El SKU '" + product.getSku() + "' ya está registrado en otro producto.");
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // 3. PUT para actualización completa - Añadida validación de unicidad de SKU
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        return productRepository.findById(id).map(product -> {

            // Validación de unicidad de SKU
            if (productRepository.existsBySkuAndIdNot(productDetails.getSku(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El SKU '" + productDetails.getSku() + "' ya está registrado en otro producto.");
            }

            // Aplicación de cambios
            product.setSku(productDetails.getSku());
            product.setName(productDetails.getName());
            product.setBrandId(productDetails.getBrandId());
            product.setPrice(productDetails.getPrice());
            product.setImageUrl(productDetails.getImageUrl());
            product.setMinStockThreshold(productDetails.getMinStockThreshold());
            product.setCategoryId(productDetails.getCategoryId());
            product.setUnitOfMeasureId(productDetails.getUnitOfMeasureId());

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE para eliminación
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            // Nota de diseño: La eliminación de un producto con transacciones asociadas (InventoryItem, ProductSupplier)
            // causará un error de ConstraintViolationException en la DB. En una aplicación real, se preferiría
            // marcar el producto como INACTIVO en lugar de borrarlo, o forzar la eliminación de sus ítems de inventario
            // y sus relaciones con proveedores (ProductSupplier) primero.
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. GET para obtener el stock total
    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<Integer> getProductTotalStock(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Se asume que este método existe en InventoryItemRepository y retorna el stock agregado.
        Integer totalStock = inventoryItemRepository.calculateTotalStockByProductId(id);
        return ResponseEntity.ok(totalStock != null ? totalStock : 0);
    }
}
