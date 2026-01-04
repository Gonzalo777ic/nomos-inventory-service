package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductAttributeValue;
import com.nomos.inventory.service.model.ProductAttributeValue.ProductAttributeValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, ProductAttributeValueId> {

    
    List<ProductAttributeValue> findByProductId(Long productId);

    
    List<ProductAttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);
}
