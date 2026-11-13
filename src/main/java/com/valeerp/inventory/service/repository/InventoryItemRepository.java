package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.product.id = :productId")
    Integer calculateTotalStockByProductId(@Param("productId") Long productId);

    List<InventoryItem> findByProductId(Long productId);

    List<InventoryItem> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    Optional<InventoryItem> findByProductIdAndWarehouseIdAndInternalLocationIdAndLotNumberAndSerialNumber(
            Long productId,
            Long warehouseId,
            Long internalLocationId,
            String lotNumber,
            String serialNumber
    );

    /** Compatibilidad con código que aún no nombra ubicación/serie. */
    Optional<InventoryItem> findByProductIdAndWarehouseIdAndLotNumber(
            Long productId, Long warehouseId, String lotNumber);
}
