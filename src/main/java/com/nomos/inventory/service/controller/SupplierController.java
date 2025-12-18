package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Supplier;
import com.nomos.inventory.service.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de proveedores (Supplier).
 * Base URL: /api/v1/suppliers
 *
 * NOTA: La lógica de negocio se maneja directamente en el controlador
 * para seguir la arquitectura del proyecto existente (sin capa de Service).
 */
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    /**
     * GET /api/v1/suppliers : Obtener todos los proveedores.
     */
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return ResponseEntity.ok(suppliers);
    }

    /**
     * GET /api/v1/suppliers/{id} : Obtener un proveedor por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proveedor con ID " + id + " no encontrado")
                );
        return ResponseEntity.ok(supplier);
    }

    /**
     * POST /api/v1/suppliers : Crear un nuevo proveedor.
     * Incluye validación de unicidad del taxId.
     */
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {

        if (supplierRepository.findByTaxId(supplier.getTaxId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un proveedor con el taxId: " + supplier.getTaxId()
            );
        }

        Supplier createdSupplier = supplierRepository.save(supplier);
        return new ResponseEntity<>(createdSupplier, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/suppliers/{id} : Actualizar un proveedor existente.
     * Incluye validación de unicidad del taxId si cambia.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplierDetails) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Proveedor con ID " + id + " no encontrado para actualizar")
                );

        if (!supplier.getTaxId().equals(supplierDetails.getTaxId())) {
            Optional<Supplier> existingSupplier = supplierRepository.findByTaxId(supplierDetails.getTaxId());
            if (existingSupplier.isPresent() && !existingSupplier.get().getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "El nuevo taxId ya está asignado a otro proveedor."
                );
            }
        }

        supplier.setName(supplierDetails.getName());
        supplier.setTaxId(supplierDetails.getTaxId());
        supplier.setEmail(supplierDetails.getEmail());
        supplier.setPhone(supplierDetails.getPhone());
        supplier.setAddress(supplierDetails.getAddress());
        supplier.setContactName(supplierDetails.getContactName());

        Supplier updatedSupplier = supplierRepository.save(supplier);
        return ResponseEntity.ok(updatedSupplier);
    }

    /**
     * DELETE /api/v1/suppliers/{id} : Eliminar un proveedor.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Proveedor con ID " + id + " no encontrado para eliminar"
            );
        }

        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
