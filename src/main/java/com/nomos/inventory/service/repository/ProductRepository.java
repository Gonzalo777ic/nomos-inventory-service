package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByBarcode(String barcode);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    @Query("""
            SELECT p FROM Product p
            WHERE :search = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Product> search(@Param("search") String search, Pageable pageable);
    @Query("SELECT new com.nomos.inventory.service.model.dto.StockAlertDTO(" +
            "p.id, " +
            "p.name, " +
            "p.sku, " +
            "(SELECT MAX(pi.imageUrl) FROM ProductImage pi WHERE pi.productId = p.id AND pi.main = true), " +
            "CAST(COALESCE(SUM(i.quantity), 0) AS int), " +
            "p.minStockThreshold, " +
            "CAST((p.minStockThreshold - COALESCE(SUM(i.quantity), 0)) AS int), " +
            "'LOW') " +
            "FROM Product p " +
            "LEFT JOIN InventoryItem i ON i.product.id = p.id " +
            "WHERE p.minStockThreshold IS NOT NULL " +
            "GROUP BY p.id, p.name, p.sku, p.minStockThreshold " +
            "HAVING COALESCE(SUM(i.quantity), 0) <= p.minStockThreshold")
    List<StockAlertDTO> findProductsWithLowStock();
}
