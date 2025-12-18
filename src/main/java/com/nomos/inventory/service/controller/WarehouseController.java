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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        return warehouseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        if (warehouseRepository.existsByName(warehouse.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un almacén con el nombre: " + warehouse.getName());
        }

        if (warehouse.getIsMain()) {

            Optional<Warehouse> currentMain = warehouseRepository.findByIsMainTrue();
            currentMain.ifPresent(main -> {
                main.setIsMain(false);
                warehouseRepository.save(main);
            });
        }

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWarehouse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateWarehouse(@PathVariable Long id, @Valid @RequestBody Warehouse warehouseDetails) {
        return warehouseRepository.findById(id).map(warehouse -> {

            if (!warehouse.getName().equals(warehouseDetails.getName()) && warehouseRepository.existsByName(warehouseDetails.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya existe otro almacén con el nombre: " + warehouseDetails.getName());
            }

            if (warehouseDetails.getIsMain() && !warehouse.getIsMain()) {

                Optional<Warehouse> currentMain = warehouseRepository.findByIsMainTrue();
                currentMain.ifPresent(main -> {
                    if (!main.getId().equals(warehouse.getId())) {
                        main.setIsMain(false);
                        warehouseRepository.save(main);
                    }
                });
            } else if (!warehouseDetails.getIsMain() && warehouse.getIsMain()) {




            }

            warehouse.setName(warehouseDetails.getName());
            warehouse.setLocationAddress(warehouseDetails.getLocationAddress());
            warehouse.setIsMain(warehouseDetails.getIsMain());

            Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
            return ResponseEntity.ok(updatedWarehouse);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        if (warehouseRepository.existsById(id)) {


            warehouseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
