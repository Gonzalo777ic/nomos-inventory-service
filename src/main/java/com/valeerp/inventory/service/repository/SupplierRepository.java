package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    
    Optional<Supplier> findByTaxId(String taxId);
}
