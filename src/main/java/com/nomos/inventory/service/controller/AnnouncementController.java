package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Announcement;
import com.nomos.inventory.service.model.AnnouncementType;
import com.nomos.inventory.service.repository.AnnouncementRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de Anuncios (Announcement).
 * Base URL: /api/v1/announcements
 */
@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    @Autowired
    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    /**
     * GET /api/v1/announcements : Obtener todos los anuncios.
     */
    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();
        return ResponseEntity.ok(announcements);
    }

    /**
     * GET /api/v1/announcements/active : Obtener todos los anuncios actualmente activos y vigentes.
     * Un anuncio es activo si isActive=true y la fecha actual está entre startDate y endDate.
     */
    @GetMapping("/active")
    public ResponseEntity<List<Announcement>> getActiveAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        // Usamos el mismo 'now' para before y after para encontrar anuncios cuya vigencia cubre el momento actual
        List<Announcement> activeAnnouncements = announcementRepository
                .findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);
        return ResponseEntity.ok(activeAnnouncements);
    }

    /**
     * GET /api/v1/announcements/{id} : Obtener un anuncio por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Anuncio con ID " + id + " no encontrado")
                );
        return ResponseEntity.ok(announcement);
    }

    /**
     * POST /api/v1/announcements : Crear un nuevo anuncio.
     */
    @PostMapping
    public ResponseEntity<Announcement> createAnnouncement(@Valid @RequestBody Announcement announcement) {
        try {
            Announcement createdAnnouncement = announcementRepository.save(announcement);
            return new ResponseEntity<>(createdAnnouncement, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Capturar la excepción de validación de lógica (startDate vs endDate)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * PUT /api/v1/announcements/{id} : Actualizar un anuncio existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody Announcement announcementDetails) {
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Anuncio con ID " + id + " no encontrado para actualizar")
                );

        try {
            // Actualizar campos
            existingAnnouncement.setTitle(announcementDetails.getTitle());
            existingAnnouncement.setContent(announcementDetails.getContent());
            existingAnnouncement.setStartDate(announcementDetails.getStartDate());
            existingAnnouncement.setEndDate(announcementDetails.getEndDate());
            existingAnnouncement.setType(announcementDetails.getType());
            existingAnnouncement.setIsActive(announcementDetails.getIsActive());

            // La validación de lógica se ejecuta automáticamente antes de la actualización
            Announcement updatedAnnouncement = announcementRepository.save(existingAnnouncement);
            return ResponseEntity.ok(updatedAnnouncement);
        } catch (IllegalArgumentException e) {
            // Capturar la excepción de validación de lógica (startDate vs endDate)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * DELETE /api/v1/announcements/{id} : Eliminar un anuncio.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Anuncio con ID " + id + " no encontrado para eliminar"
            );
        }
        announcementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
