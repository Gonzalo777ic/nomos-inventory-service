package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
/**
 * Repositorio para la gestión de categorías de productos.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {



    Optional<Category> findByName(String name); // Necesario para DataLoader
}
