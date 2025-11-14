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
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory/product-suppliers")
@RequiredArgsConstructor
public class ProductSupplierController {

    private final ProductSupplierRepository psRepository;

    // --- LÓGICA AUXILIAR DE NEGOCIO ---

    /**
     * Asegura que si se establece un nuevo proveedor como preferido (isPreferred=true),
     * el proveedor preferido anterior para ese producto sea marcado como isPreferred=false.
     * * NOTA: Este método ahora asume que será llamado desde un método que tiene @Transactional.
     * @param productId ID del producto afectado.
     * @param newPreferredSupplierId ID del proveedor que será el nuevo preferido.
     */
    private void enforceSinglePreferredSupplier(Long productId, Long newPreferredSupplierId) {
        if (newPreferredSupplierId == null) {
            return;
        }

        // 1. Buscar todos los proveedores preferidos existentes para el producto.
        // Ahora el repositorio devuelve una lista (corregido).
        List<ProductSupplier> existingPreferred = psRepository.findByProductIdAndIsPreferred(productId, true);

        // 2. Desactivar todos los proveedores preferidos anteriores, excepto el nuevo.
        for (ProductSupplier ps : existingPreferred) {
            // Desactivar SOLAMENTE si no es el mismo que se va a activar
            if (!ps.getSupplierId().equals(newPreferredSupplierId)) {
                ps.setIsPreferred(false);
                psRepository.save(ps);
            }
        }
    }

    // --- ENDPOINTS ---

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
    @Transactional // 👈 Mantenemos la transacción en el método público
    public ResponseEntity<?> addProductSupplier(@Valid @RequestBody ProductSupplier relation) {

        // 🎯 Lógica de unicidad para 'isPreferred'
        if (Boolean.TRUE.equals(relation.getIsPreferred())) {
            enforceSinglePreferredSupplier(relation.getProductId(), relation.getSupplierId());
        }

        ProductSupplier savedRelation = psRepository.save(relation);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRelation);
    }

    // 3. PUT (UPDATE) - Actualizar los detalles de la relación
    @PutMapping("/{productId}/{supplierId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional // 👈 Mantenemos la transacción en el método público
    public ResponseEntity<ProductSupplier> updateProductSupplier(
            @PathVariable Long productId,
            @PathVariable Long supplierId,
            @Valid @RequestBody ProductSupplier relationDetails) {

        ProductSupplierId id = new ProductSupplierId(productId, supplierId);

        return psRepository.findById(id).map(relation -> {

            // 1. Guardar el estado de preferencia de la request
            boolean newIsPreferred = Boolean.TRUE.equals(relationDetails.getIsPreferred());

            // 2. Si la nueva relación quiere ser la preferida, forzamos la unicidad
            if (newIsPreferred) {
                enforceSinglePreferredSupplier(productId, supplierId);
            }

            // 3. Actualizar el resto de campos
            relation.setSupplierProductCode(relationDetails.getSupplierProductCode());
            relation.setUnitCost(relationDetails.getUnitCost());
            relation.setLeadTimeDays(relationDetails.getLeadTimeDays());
            relation.setIsPreferred(newIsPreferred);
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