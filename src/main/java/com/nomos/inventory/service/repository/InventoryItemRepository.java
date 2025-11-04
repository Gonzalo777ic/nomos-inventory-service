package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para manejar la entidad de existencias de inventario (lotes).
 */
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Calcula el stock total sumando 'currentStock'
     * de todos los InventoryItem asociados a un Product específico.
     */
    @Query("SELECT SUM(i.currentStock) FROM InventoryItem i WHERE i.product.id = :productId")
    Integer calculateTotalStockByProductId(@Param("productId") Long productId);

    /**
     * Obtener todos los ítems de inventario para un producto (útil para ver lotes).
     */
    List<InventoryItem> findByProductId(Long productId);

    /**
     * Verifica la unicidad de un lote (product, warehouse, lotNumber)
     * y excluye el ID del ítem actual si se está actualizando.
     */
    Optional<InventoryItem> findByProductIdAndWarehouseIdAndLotNumber(Long productId, Long warehouseId, String lotNumber);
}
