package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    // Puedes añadir métodos de búsqueda personalizados si son necesarios.
    Optional<Brand> findByNameIgnoreCase(String name);
}