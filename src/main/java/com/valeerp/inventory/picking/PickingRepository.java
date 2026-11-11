package com.valeerp.inventory.picking;

import com.nomos.inventory.service.model.PickingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickingRepository extends JpaRepository<Picking, Long> {

    List<Picking> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Picking> findBySourceWarehouseIdOrderByCreatedAtDesc(Long warehouseId);

    List<Picking> findByStatusOrderByCreatedAtDesc(PickingStatus status);

    Optional<Picking> findByCompanyIdAndCode(Long companyId, String code);

    boolean existsByCompanyIdAndCode(Long companyId, String code);
}
