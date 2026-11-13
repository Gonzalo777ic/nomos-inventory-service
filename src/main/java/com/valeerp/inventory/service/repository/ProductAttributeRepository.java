package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    boolean existsByName(String name);
}
