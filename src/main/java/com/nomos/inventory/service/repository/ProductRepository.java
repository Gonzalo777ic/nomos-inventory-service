package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findBySku(String sku); 

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
    @Query("SELECT new com.nomos.inventory.service.model.dto.StockAlertDTO(" +
            "p.id, " +
            "p.name, " +
            "p.sku, " +
            "(SELECT MAX(pi.imageUrl) FROM ProductImage pi WHERE pi.product.id = p.id AND pi.isMain = true), " + // <--- CAMBIO AQUÍ
            "CAST(COALESCE(SUM(i.quantity), 0) AS int), " +
            "p.minStockThreshold, " +
            "CAST((p.minStockThreshold - COALESCE(SUM(i.quantity), 0)) AS int), " +
            "'LOW') " +
            "FROM Product p " +
            "LEFT JOIN InventoryItem i ON i.product.id = p.id " +
            "WHERE p.minStockThreshold IS NOT NULL " +
            "GROUP BY p.id, p.name, p.sku, p.minStockThreshold " + // Quitamos p.imageUrl del group by
            "HAVING COALESCE(SUM(i.quantity), 0) <= p.minStockThreshold")
    List<StockAlertDTO> findProductsWithLowStock();
}
