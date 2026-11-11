package com.valeerp.inventory.cyclecount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CycleCountRepository extends JpaRepository<CycleCount, Long> {

    List<CycleCount> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<CycleCount> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);

    Optional<CycleCount> findByCompanyIdAndCode(Long companyId, String code);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}
