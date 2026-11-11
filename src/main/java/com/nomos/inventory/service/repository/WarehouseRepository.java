package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByCompanyIdOrderByCodeAsc(Long companyId);

    List<Warehouse> findByCompanyIdAndBranchIdOrderByCodeAsc(Long companyId, Long branchId);

    Optional<Warehouse> findByCompanyIdAndCode(Long companyId, String code);

    @Query("select w from Warehouse w where w.companyId = :companyId and w.isMain = true")
    Optional<Warehouse> findByCompanyIdAndIsMainTrue(@Param("companyId") Long companyId);

    @Query("select w from Warehouse w where w.isMain = true")
    Optional<Warehouse> findByIsMainTrue();

    boolean existsByCompanyIdAndCode(Long companyId, String code);

    boolean existsByCompanyIdAndCodeAndIdNot(Long companyId, String code, Long id);

    boolean existsByName(String name);

    Optional<Warehouse> findByName(String name);
}
