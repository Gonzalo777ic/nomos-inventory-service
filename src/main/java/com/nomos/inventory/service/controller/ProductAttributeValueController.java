package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.ProductAttributeValue;
import com.nomos.inventory.service.model.ProductAttributeValue.ProductAttributeValueId;
import com.nomos.inventory.service.repository.ProductAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/product-attribute-values")
@RequiredArgsConstructor
public class ProductAttributeValueController {

    private final ProductAttributeValueRepository pavRepository;

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductAttributeValue>> getAttributeValuesByProduct(@PathVariable Long productId) {
        List<ProductAttributeValue> values = pavRepository.findByProductId(productId);
        return ResponseEntity.ok(values);
    }

    @GetMapping("/product/{productId}/attribute/{attributeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductAttributeValue>> getAttributeValueByProductAndAttribute(
            @PathVariable Long productId,
            @PathVariable Long attributeId) {
        List<ProductAttributeValue> values = pavRepository.findByProductIdAndAttributeId(productId, attributeId);


        return ResponseEntity.ok(values);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addProductAttributeValue(@Valid @RequestBody ProductAttributeValue attributeValue) {

        ProductAttributeValueId id = new ProductAttributeValueId(attributeValue.getProductId(), attributeValue.getAttributeId());
        if (pavRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un valor para el atributo " + attributeValue.getAttributeId() +
                            " en el producto " + attributeValue.getProductId() + ". Use PUT para actualizar.");
        }

        ProductAttributeValue savedAttributeValue = pavRepository.save(attributeValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAttributeValue);
    }

    @PutMapping("/{productId}/{attributeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductAttributeValue> updateProductAttributeValue(
            @PathVariable Long productId,
            @PathVariable Long attributeId,
            @Valid @RequestBody ProductAttributeValue attributeValueDetails) {

        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);

        return pavRepository.findById(id).map(attributeValue -> {
            attributeValue.setValue(attributeValueDetails.getValue());
            ProductAttributeValue updatedAttributeValue = pavRepository.save(attributeValue);
            return ResponseEntity.ok(updatedAttributeValue);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{productId}/{attributeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductAttributeValue(
            @PathVariable Long productId,
            @PathVariable Long attributeId) {

        ProductAttributeValueId id = new ProductAttributeValueId(productId, attributeId);

        if (pavRepository.existsById(id)) {
            pavRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
