package com.valeerp.procurement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FrameworkAgreementRepository extends JpaRepository<PurchaseFrameworkAgreement, Long> {

    boolean existsByCode(String code);

    List<PurchaseFrameworkAgreement> findBySupplierIdOrderByValidFromDesc(Long supplierId);

    @Query("""
            SELECT a FROM PurchaseFrameworkAgreement a
            LEFT JOIN FETCH a.lines
            WHERE a.id = :id
            """)
    Optional<PurchaseFrameworkAgreement> findByIdWithLines(@Param("id") Long id);

    @Query("""
            SELECT a FROM PurchaseFrameworkAgreement a
            LEFT JOIN FETCH a.lines
            WHERE a.supplierId = :supplierId
              AND a.status = com.valeerp.procurement.FrameworkAgreementStatus.ACTIVE
            """)
    List<PurchaseFrameworkAgreement> findActiveBySupplierId(@Param("supplierId") Long supplierId);
}
