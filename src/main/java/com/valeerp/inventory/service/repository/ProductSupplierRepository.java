package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.ProductSupplier;
import com.valeerp.inventory.service.model.ProductSupplier.ProductSupplierId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, ProductSupplierId> {

    
    List<ProductSupplier> findByProductId(Long productId);

    

    List<ProductSupplier> findByProductIdAndIsPreferred(Long productId, Boolean isPreferred);
}