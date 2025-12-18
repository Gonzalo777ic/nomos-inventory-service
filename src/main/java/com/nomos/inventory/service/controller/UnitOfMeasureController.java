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

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<UnitOfMeasure>> getAllUnits() {
        return ResponseEntity.ok(uomRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUnit(@Valid @RequestBody UnitOfMeasure uom) {

        if (uomRepository.existsByAbbreviation(uom.getAbbreviation())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La abreviatura '" + uom.getAbbreviation() + "' ya existe.");
        }

        UnitOfMeasure savedUom = uomRepository.save(uom);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUom);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitOfMeasure uomDetails) {
        return uomRepository.findById(id).map(uom -> {

            if (!uom.getAbbreviation().equals(uomDetails.getAbbreviation()) &&
                    uomRepository.existsByAbbreviation(uomDetails.getAbbreviation())) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La abreviatura '" + uomDetails.getAbbreviation() + "' ya existe y pertenece a otra unidad.");
            }

            uom.setName(uomDetails.getName());
            uom.setAbbreviation(uomDetails.getAbbreviation());
            UnitOfMeasure updatedUom = uomRepository.save(uom);
            return ResponseEntity.ok(updatedUom);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) {
        if (uomRepository.existsById(id)) {


            uomRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
