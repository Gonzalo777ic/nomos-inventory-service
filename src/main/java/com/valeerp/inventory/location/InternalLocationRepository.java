package com.valeerp.inventory.location;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternalLocationRepository extends JpaRepository<InternalLocation, Long> {

    List<InternalLocation> findByWarehouseIdOrderByLocationTypeAscCodeAsc(Long warehouseId);

    Optional<InternalLocation> findByWarehouseIdAndCode(Long warehouseId, String code);

    Optional<InternalLocation> findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(
            Long warehouseId, InternalLocationType locationType);

    boolean existsByWarehouseIdAndCode(Long warehouseId, String code);

    boolean existsByWarehouseIdAndCodeAndIdNot(Long warehouseId, String code, Long id);

    long countByWarehouseId(Long warehouseId);
}
