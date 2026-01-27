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

}
