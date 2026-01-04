package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    
    List<InventoryMovement> findByProductIdOrderByMovementDateDesc(Long productId);

    
    List<InventoryMovement> findByReferenceIdAndReferenceService(Long referenceId, String referenceService);
}
