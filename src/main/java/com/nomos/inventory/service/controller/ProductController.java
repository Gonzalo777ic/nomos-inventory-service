package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// Importante: Necesitas inyectar el nuevo repositorio si fueras a manejar movimientos aquí,
// pero por ahora solo ajustamos el ProductController

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

    // 2. POST (CREATE)
    // El producto se crea SIN stock inicial.
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // En este punto, el frontend debería redirigir al formulario de 'StockEntry'
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    // 3. PUT para actualización completa
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id).map(product -> {

            product.setSku(productDetails.getSku());
            product.setName(productDetails.getName());

            // 🎯 CAMBIO: Usar getBrand/setBrand
            product.setBrand(productDetails.getBrand());

            product.setPrice(productDetails.getPrice());
            // 🛑 ELIMINADO: No se actualiza el stock aquí:
            // product.setStock(productDetails.getStock());

            product.setImageUrl(productDetails.getImageUrl());
            product.setSupplier(productDetails.getSupplier());

            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE para eliminación
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            // Nota: En un sistema real, se debe verificar si hay StockEntries
            // asociadas y eliminarlas o marcarlas como inactivas primero.
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<Integer> getProductTotalStock(@PathVariable Long id) {
        // Verifica si el producto existe
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Ejecuta la consulta SUM(currentStock)
        Integer totalStock = inventoryItemRepository.calculateTotalStockByProductId(id);

        // Si no hay ítems de inventario, el resultado es NULL, lo mapeamos a 0.
        return ResponseEntity.ok(totalStock != null ? totalStock : 0);
    }
}
