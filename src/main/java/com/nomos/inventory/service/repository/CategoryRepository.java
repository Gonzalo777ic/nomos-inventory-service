package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la gestión de categorías de productos.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Puedes añadir métodos específicos aquí si necesitas buscar por nombre, por ejemplo.
    // List<Category> findByNameContaining(String name);
}
