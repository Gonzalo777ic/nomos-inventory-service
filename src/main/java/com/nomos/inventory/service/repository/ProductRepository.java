package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

// No se necesita implementación, solo la interfaz
public interface ProductRepository extends JpaRepository<Product, Long> {
}