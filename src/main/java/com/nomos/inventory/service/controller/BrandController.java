package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Brand;
import com.nomos.inventory.service.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brands")
// Permite solicitudes del frontend (client)
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BrandController {

    private final BrandRepository brandRepository;

    @Autowired
    public BrandController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    // GET /api/brands - Obtener todas las marcas
    @GetMapping
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // GET /api/brands/{id} - Obtener marca por ID
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        Optional<Brand> brand = brandRepository.findById(id);
        return brand.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/brands - Crear una nueva marca
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand) {
        // Validación básica (mejorar con Service Layer y validación de unicidad)
        if (brand.getName() == null || brand.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Brand savedBrand = brandRepository.save(brand);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBrand);
    }

    // PUT /api/brands/{id} - Actualizar una marca existente
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        Optional<Brand> brandOptional = brandRepository.findById(id);

        if (brandOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Brand brand = brandOptional.get();
        brand.setName(brandDetails.getName());
        brand.setCode(brandDetails.getCode());
        brand.setWebsite(brandDetails.getWebsite());
        brand.setLogoUrl(brandDetails.getLogoUrl());

        Brand updatedBrand = brandRepository.save(brand);
        return ResponseEntity.ok(updatedBrand);
    }

    // DELETE /api/brands/{id} - Eliminar una marca
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        if (!brandRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        brandRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}