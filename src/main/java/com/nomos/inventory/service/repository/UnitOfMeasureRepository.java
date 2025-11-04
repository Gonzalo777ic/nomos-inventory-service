package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad UnitOfMeasure.
 */
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
    // Spring Data JPA generará automáticamente los métodos CRUD básicos.
    // Adicionalmente, podemos buscar por abreviatura para validaciones.
    boolean existsByAbbreviation(String abbreviation);
}
