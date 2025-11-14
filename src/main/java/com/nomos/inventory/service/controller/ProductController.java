package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.repository.ProductSupplierRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.ProductSupplier; // Importar ProductSupplier para usarlo
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.ProductImageRepository;
// 🔑 IMPORTACIONES DE REPOSITORIOS DE MAESTROS
import com.nomos.inventory.service.repository.BrandRepository;
import com.nomos.inventory.service.repository.CategoryRepository;
import com.nomos.inventory.service.repository.UnitOfMeasureRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;

    // 🔑 INYECCIÓN DE DEPENDENCIAS DE REPOSITORIOS DE MAESTROS
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    // Repositorios de relaciones
    private final ProductSupplierRepository productSupplierRepository;
    private final SupplierRepository supplierRepository;

    // 🔑 INYECCIÓN DEL REPOSITORIO DE IMÁGENES
    private final ProductImageRepository productImageRepository;


    // 1. GET (READ ALL) - Modificado para enriquecer la respuesta
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productRepository.findAll();

        // 🔑 Mapeamos y enriquecemos cada producto con sus nombres de FKs e IMAGEN.
        List<Product> enrichedProducts = products.stream()
                .map(this::enrichProductWithNames)
                .collect(Collectors.toList());

        return ResponseEntity.ok(enrichedProducts);
    }

    // 2. POST (CREATE) - Modificado para enriquecer la respuesta
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El SKU '" + product.getSku() + "' ya está registrado en otro producto.");
        }

        Product savedProduct = productRepository.save(product);
        // 🔑 Enriquecemos el producto recién creado antes de devolverlo
        return ResponseEntity.status(HttpStatus.CREATED).body(enrichProductWithNames(savedProduct));
    }

    // 3. PUT para actualización completa - Modificado para enriquecer la respuesta
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

            // ❌ ELIMINADO: Ya no se acepta imageUrl en el payload de Product, se maneja por ProductImageController
            // product.setImageUrl(productDetails.getImageUrl());

            product.setMinStockThreshold(productDetails.getMinStockThreshold());
            product.setCategoryId(productDetails.getCategoryId());
            product.setUnitOfMeasureId(productDetails.getUnitOfMeasureId());

            Product updatedProduct = productRepository.save(product);
            // 🔑 Enriquecemos el producto actualizado antes de devolverlo
            return ResponseEntity.ok(enrichProductWithNames(updatedProduct));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE para eliminación (No necesita enriquecimiento)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. GET para obtener el stock total (No necesita enriquecimiento)
    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<Integer> getProductTotalStock(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Integer totalStock = inventoryItemRepository.calculateTotalStockByProductId(id);
        return ResponseEntity.ok(totalStock != null ? totalStock : 0);
    }

    /**
     * Método auxiliar para rellenar los campos @Transient (String names) de un producto
     * y la imagen principal.
     */
    private Product enrichProductWithNames(Product product) {
        // 1. Marca, Categoría, UoM
        brandRepository.findById(product.getBrandId())
                .ifPresent(brand -> product.setBrandName(brand.getName()));

        categoryRepository.findById(product.getCategoryId())
                .ifPresent(category -> product.setCategoryName(category.getName()));

        unitOfMeasureRepository.findById(product.getUnitOfMeasureId())
                .ifPresent(uom -> product.setUnitOfMeasureName(uom.getName()));

        // 🔑 4. PROVEEDOR (Lógica de proveedor preferido)
        // El repositorio ahora devuelve List<ProductSupplier> (corregido en el paso anterior).
        List<ProductSupplier> preferredSuppliers = productSupplierRepository.findByProductIdAndIsPreferred(product.getId(), true);

        // Verificamos si la lista no está vacía y tomamos el primer elemento.
        if (!preferredSuppliers.isEmpty()) {
            // Esto es seguro ya que la lista no está vacía
            ProductSupplier preferredSupplier = preferredSuppliers.get(0);

            Long supplierId = preferredSupplier.getSupplierId(); // Ahora sí podemos llamar a getSupplierId()
            supplierRepository.findById(supplierId)
                    .ifPresent(supplier -> product.setSupplierName(supplier.getName()));
        }

        // 🔑 5. IMAGEN PRINCIPAL (A través de la nueva tabla ProductImage)
        productImageRepository.findByProductIdAndIsMain(product.getId(), true)
                .ifPresentOrElse(
                        // Si encuentra la principal
                        mainImage -> product.setImageUrl(mainImage.getImageUrl()),
                        // Si no encuentra la principal, busca la primera por orden
                        () -> productImageRepository.findTopByProductIdOrderBySortOrderAsc(product.getId())
                                .ifPresent(firstImage -> product.setImageUrl(firstImage.getImageUrl()))
                );

        return product;
    }
}