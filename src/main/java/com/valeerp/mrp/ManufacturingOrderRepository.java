package com.valeerp.mrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {

    boolean existsByCode(String code);

    List<ManufacturingOrder> findByWarehouseIdOrderByIdDesc(Long warehouseId);

    List<ManufacturingOrder> findByProductIdOrderByIdDesc(Long productId);

    @Query("""
            SELECT o FROM ManufacturingOrder o
            LEFT JOIN FETCH o.componentLines
            WHERE o.id = :id
            """)
    Optional<ManufacturingOrder> findByIdWithLines(@Param("id") Long id);
}
