package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Brand;
import com.nomos.inventory.service.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/masters/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    // 1. GET (READ ALL)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandRepository.findAll());
    }

    // 2. POST (CREATE)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createBrand(@Valid @RequestBody Brand brand) {
        if (brandRepository.existsByName(brand.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una marca con el nombre '" + brand.getName() + "'.");
        }
        if (brandRepository.existsByCode(brand.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una marca con el código '" + brand.getCode() + "'.");
        }
        Brand savedBrand = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBrand);
    }

    // 3. PUT (UPDATE)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateBrand(@PathVariable Long id, @Valid @RequestBody Brand brandDetails) {
        return brandRepository.findById(id).map(brand -> {
            // Validar unicidad del nombre, excluyendo la propia marca
            if (brandRepository.existsByNameAndIdNot(brandDetails.getName(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe otra marca con el nombre '" + brandDetails.getName() + "'.");
            }
            // Validar unicidad del código, excluyendo la propia marca
            if (brandRepository.existsByCodeAndIdNot(brandDetails.getCode(), id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe otra marca con el código '" + brandDetails.getCode() + "'.");
            }

            brand.setName(brandDetails.getName());
            brand.setCode(brandDetails.getCode());
            brand.setWebsite(brandDetails.getWebsite());
            brand.setLogoUrl(brandDetails.getLogoUrl());

            Brand updatedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(updatedBrand);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        if (brandRepository.existsById(id)) {
            // Consideraciones para la eliminación:
            // En un sistema real, se debe verificar si la marca está siendo utilizada por algún Producto
            // antes de permitir la eliminación. Podría ser necesario ponerla inactiva o requerir que
            // todos los productos asociados se reasignen a otra marca.
            brandRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
