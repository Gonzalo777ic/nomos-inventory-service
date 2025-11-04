package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * Entidad que define el horario de atención semanal regular de la tienda.
 */
@Entity
@Table(name = "store_schedules",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"dayOfWeek"})}) // Un horario por día
@Data
public class StoreSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true) // Asegura que solo haya una entrada por día
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime openTime;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime closeTime;

    @NotNull(message = "Indicar si está abierto es obligatorio")
    private Boolean isOpen;

    // Constructor sin argumentos requerido por JPA
    public StoreSchedule() {
        this.isOpen = true; // Por defecto, se asume que está abierto si se define un horario
    }
}
