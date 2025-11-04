package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ClosureDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repositorio para la entidad ClosureDate.
 */
@Repository
public interface ClosureDateRepository extends JpaRepository<ClosureDate, Long> {

    /**
     * Busca un cierre programado por una fecha específica.
     */
    Optional<ClosureDate> findByClosureDate(LocalDate closureDate);
}
