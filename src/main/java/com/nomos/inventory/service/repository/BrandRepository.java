package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad Brand.
 */
public interface BrandRepository extends JpaRepository<Brand, Long> {
    // Métodos para verificar unicidad de nombre y código
    boolean existsByName(String name);
    boolean existsByCode(String code);

    // Métodos para verificar unicidad excluyendo el ID actual (para actualizaciones)
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCodeAndIdNot(String code, Long id);
}
