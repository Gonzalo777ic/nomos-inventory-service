package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Repositorio para manejar la entidad de existencias de inventario
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * 🎯 CONSULTA CLAVE: Calcula el stock total sumando 'currentStock'
     * de todos los InventoryItem asociados a un Product específico.
     * Esta es la forma de "ver" el stock total.
     */
    @Query("SELECT SUM(i.currentStock) FROM InventoryItem i WHERE i.product.id = :productId")
    Integer calculateTotalStockByProductId(@Param("productId") Long productId);

    // Obtener todos los ítems de inventario para un producto (útil para ver lotes)
    List<InventoryItem> findByProductId(Long productId);
}