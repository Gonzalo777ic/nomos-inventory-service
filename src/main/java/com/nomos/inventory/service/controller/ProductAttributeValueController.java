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

    // 1. GET ALL by Product (Lo más común es obtener los atributos de un producto)
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductAttributeValue>> getAttributeValuesByProduct(@PathVariable Long productId) {
        List<ProductAttributeValue> values = pavRepository.findByProductId(productId);
        return ResponseEntity.ok(values);
    }

    // 2. GET ONE by Product and Attribute (Obtener un atributo específico de un producto)
    @GetMapping("/product/{productId}/attribute/{attributeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductAttributeValue>> getAttributeValueByProductAndAttribute(
            @PathVariable Long productId,
            @PathVariable Long attributeId) {
        List<ProductAttributeValue> values = pavRepository.findByProductIdAndAttributeId(productId, attributeId);
        // Retornamos una lista ya que podrías permitir múltiples valores para un mismo atributo si el diseño lo cambiara,
        // pero con la uniqueConstraint actual solo habrá 0 o 1 elemento.
        return ResponseEntity.ok(values);
    }

    // 3. POST (CREATE) - Añadir un valor de atributo a un producto
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> addProductAttributeValue(@Valid @RequestBody ProductAttributeValue attributeValue) {

        // Verifica si ya existe un valor para este producto y atributo
        ProductAttributeValueId id = new ProductAttributeValueId(attributeValue.getProductId(), attributeValue.getAttributeId());
        if (pavRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un valor para el atributo " + attributeValue.getAttributeId() +
                            " en el producto " + attributeValue.getProductId() + ". Use PUT para actualizar.");
        }

        ProductAttributeValue savedAttributeValue = pavRepository.save(attributeValue);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAttributeValue);
    }

    // 4. PUT (UPDATE) - Actualizar el valor de un atributo para un producto
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

    // 5. DELETE (DELETE) - Eliminar un valor de atributo de un producto
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
