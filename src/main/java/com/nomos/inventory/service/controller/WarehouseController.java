package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Warehouse;
import com.nomos.inventory.service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    // 1. GET (READ ALL)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseRepository.findAll());
    }

    // 2. GET (READ ONE)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        return warehouseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST (CREATE) - Lógica de Almacén Principal
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        if (warehouseRepository.existsByName(warehouse.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un almacén con el nombre: " + warehouse.getName());
        }

        if (warehouse.getIsMain()) {
            // Si el nuevo almacén se marca como principal, desactivar el actual principal
            Optional<Warehouse> currentMain = warehouseRepository.findByIsMainTrue();
            currentMain.ifPresent(main -> {
                main.setIsMain(false);
                warehouseRepository.save(main);
            });
        }

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWarehouse);
    }

    // 4. PUT (UPDATE) - Lógica de Almacén Principal
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long id, @Valid @RequestBody Warehouse warehouseDetails) {
        return warehouseRepository.findById(id).map(warehouse -> {

            // 4.1. Validación de Unicidad de Nombre
            if (!warehouse.getName().equals(warehouseDetails.getName()) && warehouseRepository.existsByName(warehouseDetails.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe otro almacén con el nombre: " + warehouseDetails.getName());
            }

            // 4.2. Lógica de Almacén Principal
            if (warehouseDetails.getIsMain() && !warehouse.getIsMain()) {
                // Si el almacén se está marcando como principal, desactivar el actual principal (si existe)
                Optional<Warehouse> currentMain = warehouseRepository.findByIsMainTrue();
                currentMain.ifPresent(main -> {
                    if (!main.getId().equals(warehouse.getId())) {
                        main.setIsMain(false);
                        warehouseRepository.save(main);
                    }
                });
            } else if (!warehouseDetails.getIsMain() && warehouse.getIsMain()) {
                // Si el almacén principal se está desactivando, debemos prevenir que no quede ningún principal.
                // Es mejor permitirlo y forzar al usuario a crear/marcar uno nuevo después,
                // pero por robustez, se recomienda al menos un almacén principal.
                // Aquí lo permitimos, pero el sistema debe manejar el caso de 'ningún principal'.
            }

            // 4.3. Actualización de Campos
            warehouse.setName(warehouseDetails.getName());
            warehouse.setLocationAddress(warehouseDetails.getLocationAddress());
            warehouse.setIsMain(warehouseDetails.getIsMain());

            Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
            return ResponseEntity.ok(updatedWarehouse);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        if (warehouseRepository.existsById(id)) {
            // Nota: En un sistema real, se debería verificar si existen InventoryItems
            // asociados a este almacén antes de eliminarlo.
            warehouseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
