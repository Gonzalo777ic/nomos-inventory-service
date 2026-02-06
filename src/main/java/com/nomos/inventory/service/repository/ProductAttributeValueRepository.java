package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductAttributeValue;
import com.nomos.inventory.service.model.ProductAttributeValue.ProductAttributeValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, ProductAttributeValueId> {

    List<ProductAttributeValue> findByProductId(Long productId);

    List<ProductAttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);

    @Query("SELECT p.id as productId, p.name as productName, p.sku as productSku, pav.value as attributeValue " +
            "FROM Product p, ProductAttributeValue pav " +
            "WHERE p.id = pav.productId AND pav.attributeId = :attributeId")

    List<ProductAttributeValue> findByAttributeId(Long attributeId);

}
