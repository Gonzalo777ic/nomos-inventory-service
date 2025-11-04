package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad InventoryMovement.
 */
@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    /**
     * Busca movimientos por ID de producto.
     */
    List<InventoryMovement> findByProductIdOrderByMovementDateDesc(Long productId);

    /**
     * Busca movimientos por el ID de la referencia (Ej: todos los movimientos de una Orden de Compra específica).
     */
    List<InventoryMovement> findByReferenceIdAndReferenceService(Long referenceId, String referenceService);
}
