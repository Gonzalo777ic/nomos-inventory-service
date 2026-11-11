package com.valeerp.mrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BomRepository extends JpaRepository<BillOfMaterials, Long> {

    boolean existsByCode(String code);

    List<BillOfMaterials> findByProductIdOrderByCodeAsc(Long productId);

    @Query("""
            SELECT b FROM BillOfMaterials b
            LEFT JOIN FETCH b.lines
            WHERE b.id = :id
            """)
    Optional<BillOfMaterials> findByIdWithLines(@Param("id") Long id);

    @Query("""
            SELECT b FROM BillOfMaterials b
            LEFT JOIN FETCH b.lines
            WHERE b.productId = :productId
              AND b.status = com.valeerp.mrp.BomStatus.ACTIVE
            ORDER BY b.code ASC
            """)
    List<BillOfMaterials> findActiveByProductId(@Param("productId") Long productId);
}
