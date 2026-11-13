package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {


    boolean existsByAbbreviation(String abbreviation);
    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation); 
}
