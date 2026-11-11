package com.valeerp.inventory.reorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReorderRuleRepository extends JpaRepository<ReorderRule, Long> {

    List<ReorderRule> findByWarehouseIdOrderByProductIdAsc(Long warehouseId);

    List<ReorderRule> findByProductIdOrderByWarehouseIdAsc(Long productId);

    Optional<ReorderRule> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
