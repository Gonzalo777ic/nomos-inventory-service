package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.NoArgsConstructor; // 🔑 Añadido
import lombok.AllArgsConstructor; // 🔑 Añadido

/**
 * Entidad que registra días festivos o cierres programados, alterando el StoreSchedule regular.
 */
@Entity
@Table(name = "closure_dates",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"closureDate"})}) // Un solo evento por día
@Data
@NoArgsConstructor // 🔑 Añadido
@AllArgsConstructor // 🔑 Añadido
public class ClosureDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de cierre es obligatoria")
    @FutureOrPresent(message = "La fecha de cierre debe ser en el presente o futuro")
    @Column(unique = true, nullable = false)
    private LocalDate closureDate;

    @NotBlank(message = "La razón del cierre es obligatoria")
    private String reason; // Ej: Feriado, Inventario Anual

    @NotNull(message = "Indicar si es cierre de día completo es obligatorio")
    private Boolean isFullDay; // Si es true, el cierre aplica a todo el día.

    @Column(nullable = true)
    private LocalTime closingTime; // Hora de cierre, si isFullDay es false (cierre parcial).


    /**
     * Lógica de validación manual para asegurar consistencia entre isFullDay y closingTime.
     */
    @PrePersist
    @PreUpdate
    private void validateClosureTime() {
        if (!isFullDay && closingTime == null) {
            throw new IllegalArgumentException("Si el cierre no es de día completo (isFullDay=false), 'closingTime' debe estar especificado.");
        }
        if (isFullDay && closingTime != null) {

            closingTime = null;
        }
    }
}