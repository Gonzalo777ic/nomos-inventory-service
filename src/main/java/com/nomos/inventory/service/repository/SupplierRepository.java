package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    
    Optional<Supplier> findByTaxId(String taxId);
}
