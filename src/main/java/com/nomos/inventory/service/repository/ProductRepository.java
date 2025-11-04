package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad Product.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Método para verificar si un SKU ya existe (para creación)
    boolean existsBySku(String sku);

    // Método para verificar si un SKU existe en otro producto (para actualización)
    boolean existsBySkuAndIdNot(String sku, Long id);
}
