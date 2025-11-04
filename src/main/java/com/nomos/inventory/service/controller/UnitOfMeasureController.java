package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.UnitOfMeasure;
import com.nomos.inventory.service.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory/units-of-measure")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureRepository uomRepository;

    // 1. GET (READ ALL) - Permite acceso amplio ya que son datos maestros.
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<UnitOfMeasure>> getAllUnits() {
        return ResponseEntity.ok(uomRepository.findAll());
    }

    // 2. POST (CREATE) - Restringido a administradores.
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUnit(@Valid @RequestBody UnitOfMeasure uom) {
        // Validación adicional para la unicidad de la abreviatura antes de guardar
        if (uomRepository.existsByAbbreviation(uom.getAbbreviation())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La abreviatura '" + uom.getAbbreviation() + "' ya existe.");
        }

        UnitOfMeasure savedUom = uomRepository.save(uom);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUom);
    }

    // 3. PUT (UPDATE) - Restringido a administradores.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitOfMeasure uomDetails) {
        return uomRepository.findById(id).map(uom -> {

            // 1. Validar unicidad de la abreviatura si se está cambiando
            if (!uom.getAbbreviation().equals(uomDetails.getAbbreviation()) &&
                    uomRepository.existsByAbbreviation(uomDetails.getAbbreviation())) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La abreviatura '" + uomDetails.getAbbreviation() + "' ya existe y pertenece a otra unidad.");
            }

            // 2. Aplicar cambios y guardar
            uom.setName(uomDetails.getName());
            uom.setAbbreviation(uomDetails.getAbbreviation());
            UnitOfMeasure updatedUom = uomRepository.save(uom);
            return ResponseEntity.ok(updatedUom);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. DELETE - Restringido a administradores.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        if (uomRepository.existsById(id)) {
            // Nota: En un entorno de producción, se debe verificar si la UoM está
            // siendo utilizada por algún Producto antes de permitir la eliminación.
            uomRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
