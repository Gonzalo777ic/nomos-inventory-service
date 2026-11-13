package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.ProductAttributeValue;
import com.valeerp.inventory.service.model.ProductAttributeValue.ProductAttributeValueId;
import com.valeerp.inventory.service.model.dto.ProductWithAttributeDTO; // Importar la interfaz
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, ProductAttributeValueId> {

    List<ProductAttributeValue> findByProductId(Long productId);
    List<ProductAttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);

    @Query("SELECT p.id as productId, p.name as productName, p.sku as productSku, pav.value as attributeValue " +
            "FROM Product p, ProductAttributeValue pav " +
            "WHERE p.id = pav.productId AND pav.attributeId = :attributeId")
    List<ProductWithAttributeDTO> findProductsByAttributeId(@Param("attributeId") Long attributeId);
}