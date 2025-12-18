package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio para la entidad UnitOfMeasure.
 */
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {


    boolean existsByAbbreviation(String abbreviation);
    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation); 
}
