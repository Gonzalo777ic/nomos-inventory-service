package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);
    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Brand> findByName(String name); 
}
