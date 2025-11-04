package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.ClosureDate;
import com.nomos.inventory.service.repository.ClosureDateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para la gestión de Días de Cierre Programado (ClosureDate).
 * Base URL: /api/v1/closure-dates
 */
@RestController
@RequestMapping("/api/v1/closure-dates")
public class ClosureDateController {

    private final ClosureDateRepository closureDateRepository;

    @Autowired
    public ClosureDateController(ClosureDateRepository closureDateRepository) {
        this.closureDateRepository = closureDateRepository;
    }

    /**
     * GET /api/v1/closure-dates : Obtener todos los cierres programados.
     */
    @GetMapping
    public ResponseEntity<List<ClosureDate>> getAllClosureDates() {
        List<ClosureDate> closures = closureDateRepository.findAll();
        return ResponseEntity.ok(closures);
    }

    /**
     * GET /api/v1/closure-dates/{id} : Obtener un cierre por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClosureDate> getClosureDateById(@PathVariable Long id) {
        ClosureDate closure = closureDateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cierre programado con ID " + id + " no encontrado")
                );
        return ResponseEntity.ok(closure);
    }

    /**
     * POST /api/v1/closure-dates : Registrar un nuevo cierre programado.
     */
    @PostMapping
    public ResponseEntity<ClosureDate> createClosureDate(@Valid @RequestBody ClosureDate closure) {
        try {
            // La validación de lógica (isFullDay vs closingTime) se realiza en @PrePersist/@PreUpdate del modelo.
            // La validación de unicidad por fecha se maneja por la constraint en la tabla y JPA.
            ClosureDate createdClosure = closureDateRepository.save(closure);
            return new ResponseEntity<>(createdClosure, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Capturar la excepción de validación de lógica del modelo
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Capturar errores de unicidad de fecha
            if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe un cierre programado para esta fecha."
                );
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear el cierre: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/closure-dates/{id} : Actualizar un cierre programado existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClosureDate> updateClosureDate(@PathVariable Long id, @Valid @RequestBody ClosureDate closureDetails) {
        ClosureDate existingClosure = closureDateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cierre programado con ID " + id + " no encontrado para actualizar")
                );

        try {
            existingClosure.setClosureDate(closureDetails.getClosureDate());
            existingClosure.setReason(closureDetails.getReason());
            existingClosure.setIsFullDay(closureDetails.getIsFullDay());
            existingClosure.setClosingTime(closureDetails.getClosingTime());

            // La validación de lógica se ejecuta automáticamente antes de la actualización
            ClosureDate updatedClosure = closureDateRepository.save(existingClosure);
            return ResponseEntity.ok(updatedClosure);
        } catch (IllegalArgumentException e) {
            // Capturar la excepción de validación de lógica del modelo
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Capturar errores de unicidad de fecha si se intenta cambiar la fecha a una ya existente
            if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe un cierre programado para la nueva fecha especificada."
                );
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar el cierre: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/closure-dates/{id} : Eliminar un cierre programado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClosureDate(@PathVariable Long id) {
        if (!closureDateRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Cierre programado con ID " + id + " no encontrado para eliminar"
            );
        }
        closureDateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
