package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.model.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findBySupplierId(Long supplierId);
    List<Quotation> findByStatusNot(QuotationStatus status); 

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.details WHERE q.id = :id")
    Optional<Quotation> findByIdWithDetails(Long id);

    @Query("SELECT DISTINCT q FROM Quotation q LEFT JOIN FETCH q.supplier LEFT JOIN FETCH q.details d LEFT JOIN FETCH d.product WHERE q.id IN :ids")
    List<Quotation> findAllWithDetailsByIdIn(@Param("ids") List<Long> ids);
}
