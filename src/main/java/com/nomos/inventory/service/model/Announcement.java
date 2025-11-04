package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que registra mensajes informativos (anuncios, avisos de feriados, promociones)
 * para ser mostrados en la tienda online.
 */
@Entity
@Table(name = "announcements")
@Data
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no debe exceder los 100 caracteres")
    private String title;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Column(columnDefinition = "TEXT")
    private String content; // El mensaje completo para el modal/banner

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endDate;

    @NotNull(message = "El tipo de anuncio es obligatorio")
    @Enumerated(EnumType.STRING)
    private AnnouncementType type; // BANNER, MODAL, POPUP

    private Boolean isActive; // Control para activar/desactivar sin cambiar fechas

    // Constructor sin argumentos requerido por JPA
    public Announcement() {
        this.isActive = true;
    }

    /**
     * Lógica de validación manual para asegurar que la fecha de inicio sea anterior a la de fin.
     */
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate == null || endDate == null) {
            return; // La anotación @NotNull ya manejará esto
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio (startDate) no puede ser posterior a la fecha de fin (endDate).");
        }
    }
}
