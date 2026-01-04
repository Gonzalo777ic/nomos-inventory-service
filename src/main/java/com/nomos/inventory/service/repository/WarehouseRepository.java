package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByIsMainTrue();

    Optional<Warehouse> findByName(String name); 

    boolean existsByName(String name);
}
