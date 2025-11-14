package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio para la entidad Product.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findBySku(String sku); // Necesario para DataLoader

    // Método para verificar si un SKU ya existe (para creación)
    boolean existsBySku(String sku);

    // Método para verificar si un SKU existe en otro producto (para actualización)
    boolean existsBySkuAndIdNot(String sku, Long id);
}
