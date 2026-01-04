package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    
    @Query("SELECT SUM(i.currentStock) FROM InventoryItem i WHERE i.product.id = :productId")
    Integer calculateTotalStockByProductId(@Param("productId") Long productId);

    
    List<InventoryItem> findByProductId(Long productId);

    
    Optional<InventoryItem> findByProductIdAndWarehouseIdAndLotNumber(Long productId, Long warehouseId, String lotNumber);
}
