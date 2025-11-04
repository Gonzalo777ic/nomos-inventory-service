package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la entidad PurchaseOrder.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    // Métodos de consulta personalizados pueden agregarse aquí si son necesarios
}
