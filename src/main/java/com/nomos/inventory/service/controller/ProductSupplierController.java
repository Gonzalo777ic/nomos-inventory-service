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


        List<ProductSupplier> existingPreferred = psRepository.findByProductIdAndIsPreferred(productId, true);

        for (ProductSupplier ps : existingPreferred) {

            if (!ps.getSupplierId().equals(newPreferredSupplierId)) {
                ps.setIsPreferred(false);
                psRepository.save(ps);
            }
        }
    }


    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductSupplier>> getSuppliersByProduct(@PathVariable Long productId) {
        List<ProductSupplier> relations = psRepository.findByProductId(productId);
        return ResponseEntity.ok(relations);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional 
    public ResponseEntity<?> addProductSupplier(@Valid @RequestBody ProductSupplier relation) {

        if (Boolean.TRUE.equals(relation.getIsPreferred())) {
            enforceSinglePreferredSupplier(relation.getProductId(), relation.getSupplierId());
        }

        ProductSupplier savedRelation = psRepository.save(relation);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRelation);
    }

    @PutMapping("/{productId}/{supplierId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional 
    public ResponseEntity<ProductSupplier> updateProductSupplier(
            @PathVariable Long productId,
            @PathVariable Long supplierId,
            @Valid @RequestBody ProductSupplier relationDetails) {

        ProductSupplierId id = new ProductSupplierId(productId, supplierId);

        return psRepository.findById(id).map(relation -> {

            boolean newIsPreferred = Boolean.TRUE.equals(relationDetails.getIsPreferred());

            if (newIsPreferred) {
                enforceSinglePreferredSupplier(productId, supplierId);
            }

            relation.setSupplierProductCode(relationDetails.getSupplierProductCode());
            relation.setUnitCost(relationDetails.getUnitCost());
            relation.setLeadTimeDays(relationDetails.getLeadTimeDays());
            relation.setIsPreferred(newIsPreferred);
            relation.setIsActive(relationDetails.getIsActive());

            ProductSupplier updatedRelation = psRepository.save(relation);
            return ResponseEntity.ok(updatedRelation);
        }).orElse(ResponseEntity.notFound().build());
    }

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