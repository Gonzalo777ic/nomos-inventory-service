package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.StoreSchedule;
import com.nomos.inventory.service.model.DayOfWeek; // Importar el enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad StoreSchedule.
 */
@Repository
public interface StoreScheduleRepository extends JpaRepository<StoreSchedule, Long> {

    /**
     * Busca un horario por un día de la semana específico.
     * @param dayOfWeek El día de la semana.
     * @return Un Optional que contiene el StoreSchedule si existe, o vacío.
     */
    Optional<StoreSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);
}
