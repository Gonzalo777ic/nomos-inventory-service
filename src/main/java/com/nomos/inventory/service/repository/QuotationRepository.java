package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Quotation;
import com.nomos.inventory.service.model.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    List<Quotation> findBySupplierId(Long supplierId);
    List<Quotation> findByStatusNot(QuotationStatus status); 

    @Query("SELECT q FROM Quotation q LEFT JOIN FETCH q.details WHERE q.id = :id")
    Optional<Quotation> findByIdWithDetails(Long id);
}
