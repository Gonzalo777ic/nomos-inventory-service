package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // 1. Obtener todas las imágenes ordenadas
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    // 2. Encontrar la imagen principal
    Optional<ProductImage> findByProductIdAndIsMain(Long productId, Boolean isMain);

    // 3. Contar cuántas imágenes existen (Usado en el Controller para la subida)
    long countByProductId(Long productId);

    // 4. 🔑 CORRECCIÓN DEL ERROR: Encontrar la primera imagen (por orden) como fallback
    Optional<ProductImage> findTopByProductIdOrderBySortOrderAsc(Long productId);
}