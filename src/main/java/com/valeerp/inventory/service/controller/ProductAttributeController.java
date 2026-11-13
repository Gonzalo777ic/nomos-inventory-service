package com.valeerp.inventory.service.controller;

import com.valeerp.inventory.service.model.ProductAttribute;
import com.valeerp.inventory.service.repository.ProductAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/attributes")
@RequiredArgsConstructor
public class ProductAttributeController {

    private final ProductAttributeRepository attributeRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductAttribute>> getAllAttributes() {
        return ResponseEntity.ok(attributeRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<ProductAttribute> getAttributeById(@PathVariable Long id) {
        return attributeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createAttribute(@Valid @RequestBody ProductAttribute attribute) {
        if (attributeRepository.existsByName(attribute.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un atributo con el nombre: " + attribute.getName());
        }
        ProductAttribute savedAttribute = attributeRepository.save(attribute);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAttribute);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateAttribute(@PathVariable Long id, @Valid @RequestBody ProductAttribute attributeDetails) {
        return attributeRepository.findById(id).map(attribute -> {

            if (!attribute.getName().equals(attributeDetails.getName()) && attributeRepository.existsByName(attributeDetails.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe otro atributo con el nombre: " + attributeDetails.getName());
            }

            attribute.setName(attributeDetails.getName());
            attribute.setDataType(attributeDetails.getDataType());

            ProductAttribute updatedAttribute = attributeRepository.save(attribute);
            return ResponseEntity.ok(updatedAttribute);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        return attributeRepository.findById(id).map(attribute -> {
            attribute.retire(com.valeerp.kernel.Actors.current(), java.time.Instant.now());
            attributeRepository.save(attribute);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
