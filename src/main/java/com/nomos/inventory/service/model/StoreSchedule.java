package com.nomos.inventory.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "store_schedules",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"dayOfWeek"})}) 
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class StoreSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true) 
    private DayOfWeek dayOfWeek;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime openTime;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime closeTime;

    @NotNull(message = "Indicar si está abierto es obligatorio")
    private Boolean isOpen;



    
    @PrePersist
    public void initializeIsOpen() {
        if (this.isOpen == null) {
            this.isOpen = true; 
        }
    }
}