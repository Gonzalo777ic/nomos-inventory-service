package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.UomConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UomConversionRepository extends JpaRepository<UomConversion, Long> {

    List<UomConversion> findAllByOrderByFromUomIdAsc();

    Optional<UomConversion> findByFromUomIdAndToUomId(Long fromUomId, Long toUomId);

    boolean existsByFromUomIdAndToUomId(Long fromUomId, Long toUomId);

    boolean existsByFromUomIdAndToUomIdAndIdNot(Long fromUomId, Long toUomId, Long id);
}
