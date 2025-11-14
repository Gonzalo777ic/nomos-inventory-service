package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio para la entidad Warehouse (Almacén).
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    // Método para encontrar el almacén principal.
    Optional<Warehouse> findByIsMainTrue();

    Optional<Warehouse> findByName(String name); // Necesario para DataLoader
    // Método para verificar si ya existe un almacén con un nombre dado.
    boolean existsByName(String name);
}
