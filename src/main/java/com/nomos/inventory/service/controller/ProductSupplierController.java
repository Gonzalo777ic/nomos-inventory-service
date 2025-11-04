package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.ProductSupplier;
import com.nomos.inventory.service.model.ProductSupplier.ProductSupplierId;
import com.nomos.inventory.service.repository.ProductSupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/product-suppliers")
@RequiredArgsConstructor
public class ProductSupplierController {

    private final ProductSupplierRepository psRepository;

    // 1. GET ALL by Product (La consulta más común)
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductSupplier>> getSuppliersByProduct(@PathVariable Long productId) {
        List<ProductSupplier> relations = psRepository.findByProductId(productId);
        return ResponseEntity.ok(relations);
    }

    // 2. POST (CREATE) - Añadir un proveedor a un producto
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addProductSupplier(@Valid @RequestBody ProductSupplier relation) {

        ProductSupplierId id = new ProductSupplierId(relation.getProductId(), relation.getSupplierId());

        // No se necesita verificar la existencia aquí, el repositorio JPA se encargará de la PK.
        // Si ya existe, lanzará una excepción, la cual debe ser manejada con un @ControllerAdvice
        // (que se implementará más adelante), pero por ahora, Spring lo maneja por defecto.

        ProductSupplier savedRelation = psRepository.save(relation);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRelation);
    }

    // 3. PUT (UPDATE) - Actualizar los detalles de la relación
    @PutMapping("/{productId}/{supplierId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductSupplier> updateProductSupplier(
            @PathVariable Long productId,
            @PathVariable Long supplierId,
            @Valid @RequestBody ProductSupplier relationDetails) {

        ProductSupplierId id = new ProductSupplierId(productId, supplierId);

        return psRepository.findById(id).map(relation -> {

            // Actualizar solo los campos que pertenecen a la relación
            relation.setSupplierProductCode(relationDetails.getSupplierProductCode());
            relation.setUnitCost(relationDetails.getUnitCost());
            relation.setLeadTimeDays(relationDetails.getLeadTimeDays());
            relation.setIsPreferred(relationDetails.getIsPreferred());
            relation.setIsActive(relationDetails.getIsActive());

            ProductSupplier updatedRelation = psRepository.save(relation);
            return ResponseEntity.ok(updatedRelation);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE (DELETE) - Eliminar la relación
    @DeleteMapping("/{productId}/{supplierId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductSupplier(
            @PathVariable Long productId,
            @PathVariable Long supplierId) {

        ProductSupplierId id = new ProductSupplierId(productId, supplierId);

        if (psRepository.existsById(id)) {
            psRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
