package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio para la entidad Supplier.
 */
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * Busca un proveedor por su identificador fiscal (taxId).
     * @param taxId Identificador fiscal único.
     * @return Un Optional que contiene el Supplier si existe.
     */
    Optional<Supplier> findByTaxId(String taxId);
}
