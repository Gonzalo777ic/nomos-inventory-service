package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 

/**
 * Entidad que registra mensajes informativos (anuncios, avisos de feriados, promociones)
 * para ser mostrados en la tienda online.
 */
@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no debe exceder los 100 caracteres")
    private String title;

    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Column(columnDefinition = "TEXT")
    private String content; 

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endDate;

    @NotNull(message = "El tipo de anuncio es obligatorio")
    @Enumerated(EnumType.STRING)
    private AnnouncementType type; 

    private Boolean isActive; 


    /**
     * Inicializa isActive a true antes de guardar y valida las fechas.
     */
    @PrePersist
    @PreUpdate
    private void validateDates() {

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (startDate == null || endDate == null) {
            return; 
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio (startDate) no puede ser posterior a la fecha de fin (endDate).");
        }
    }
}