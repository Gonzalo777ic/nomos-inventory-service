package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    Optional<ProductImage> findByProductIdAndIsMain(Long productId, Boolean isMain);

    long countByProductId(Long productId);

    Optional<ProductImage> findTopByProductIdOrderBySortOrderAsc(Long productId);
}