package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.StoreSchedule;
import com.nomos.inventory.service.model.DayOfWeek;
import com.nomos.inventory.service.repository.StoreScheduleRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Horarios de Atención (StoreSchedule).
 * Base URL: /api/v1/store-schedules
 */
@RestController
@RequestMapping("/api/v1/store-schedules")
public class StoreScheduleController {

    private final StoreScheduleRepository scheduleRepository;

    @Autowired
    public StoreScheduleController(StoreScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * GET /api/v1/store-schedules : Obtener todos los horarios definidos.
     */
    @GetMapping
    public ResponseEntity<List<StoreSchedule>> getAllSchedules() {
        List<StoreSchedule> schedules = scheduleRepository.findAll();
        return ResponseEntity.ok(schedules);
    }

    /**
     * GET /api/v1/store-schedules/{id} : Obtener un horario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StoreSchedule> getScheduleById(@PathVariable Long id) {
        StoreSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Horario con ID " + id + " no encontrado")
                );
        return ResponseEntity.ok(schedule);
    }

    /**
     * POST /api/v1/store-schedules : Crear un nuevo horario.
     * Valida que no exista un horario para el mismo día de la semana.
     */
    @PostMapping
    public ResponseEntity<StoreSchedule> createSchedule(@Valid @RequestBody StoreSchedule schedule) {

        if (scheduleRepository.findByDayOfWeek(schedule.getDayOfWeek()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un horario definido para el día " + schedule.getDayOfWeek()
            );
        }
        StoreSchedule createdSchedule = scheduleRepository.save(schedule);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/store-schedules/{id} : Actualizar un horario existente.
     * Permite cambiar el día, pero valida unicidad si se cambia.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoreSchedule> updateSchedule(@PathVariable Long id, @Valid @RequestBody StoreSchedule scheduleDetails) {
        StoreSchedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Horario con ID " + id + " no encontrado para actualizar")
                );

        if (!existingSchedule.getDayOfWeek().equals(scheduleDetails.getDayOfWeek())) {
            if (scheduleRepository.findByDayOfWeek(scheduleDetails.getDayOfWeek()).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Ya existe un horario definido para el día " + scheduleDetails.getDayOfWeek()
                );
            }
        }

        existingSchedule.setDayOfWeek(scheduleDetails.getDayOfWeek());
        existingSchedule.setOpenTime(scheduleDetails.getOpenTime());
        existingSchedule.setCloseTime(scheduleDetails.getCloseTime());
        existingSchedule.setIsOpen(scheduleDetails.getIsOpen());

        StoreSchedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return ResponseEntity.ok(updatedSchedule);
    }

    /**
     * DELETE /api/v1/store-schedules/{id} : Eliminar un horario.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Horario con ID " + id + " no encontrado para eliminar"
            );
        }
        scheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
