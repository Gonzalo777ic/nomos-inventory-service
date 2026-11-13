package com.valeerp.inventory;

import com.valeerp.inventory.service.model.Branch;
import com.valeerp.inventory.service.model.Warehouse;
import com.valeerp.inventory.service.repository.BranchRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.WarehouseDtos.CreateWarehouseRequest;
import com.valeerp.inventory.WarehouseDtos.UpdateWarehouseRequest;
import com.valeerp.inventory.WarehouseDtos.WarehouseResponse;
import com.valeerp.inventory.location.InternalLocationService;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;
    private final InternalLocationService internalLocationService;
    private final Clock clock;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            BranchRepository branchRepository,
                            InternalLocationService internalLocationService,
                            Clock clock) {
        this.warehouseRepository = warehouseRepository;
        this.branchRepository = branchRepository;
        this.internalLocationService = internalLocationService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listAll() {
        return warehouseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listByCompany(Long companyId) {
        requireCompanyId(companyId);
        return warehouseRepository.findByCompanyIdOrderByCodeAsc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> listByCompanyAndBranch(Long companyId, Long branchId) {
        requireCompanyId(companyId);
        if (branchId == null) {
            throw new DomainException("branchId es obligatorio");
        }
        return warehouseRepository.findByCompanyIdAndBranchIdOrderByCodeAsc(companyId, branchId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(Long id) {
        return toResponse(requireWarehouse(id));
    }

    @Transactional
    public WarehouseResponse create(CreateWarehouseRequest request) {
        requireCompanyId(request.companyId());
        Long branchId = resolveBranchId(request.companyId(), request.branchId());
        String code = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCompanyIdAndCode(request.companyId(), code)) {
            throw new DomainException("Ya existe un almacén con código " + code + " en la compañía");
        }
        boolean main = Boolean.TRUE.equals(request.main());
        if (main) {
            clearMainOfCompany(request.companyId(), null);
        }
        Warehouse warehouse = Warehouse.open(
                request.companyId(),
                branchId,
                code,
                request.name(),
                request.locationAddress(),
                main
        );
        Warehouse saved = warehouseRepository.save(warehouse);
        internalLocationService.ensureDefaultsForWarehouse(saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public WarehouseResponse update(Long id, UpdateWarehouseRequest request) {
        Warehouse warehouse = requireWarehouse(id);
        Long branchId = resolveBranchId(warehouse.getCompanyId(), request.branchId());
        String code = request.code().trim().toUpperCase();
        if (warehouseRepository.existsByCompanyIdAndCodeAndIdNot(warehouse.getCompanyId(), code, id)) {
            throw new DomainException("Ya existe otro almacén con código " + code + " en la compañía");
        }
        boolean main = Boolean.TRUE.equals(request.main());
        if (main && !warehouse.isMain()) {
            clearMainOfCompany(warehouse.getCompanyId(), warehouse.getId());
        }
        warehouse.relocate(branchId, code, request.name(), request.locationAddress(), main);
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void retire(Long id) {
        Warehouse warehouse = requireWarehouse(id);
        warehouse.retire(Actors.current(), Instant.now(clock));
        warehouseRepository.save(warehouse);
    }

    private void clearMainOfCompany(Long companyId, Long exceptId) {
        warehouseRepository.findByCompanyIdAndIsMainTrue(companyId).ifPresent(current -> {
            if (exceptId == null || !exceptId.equals(current.getId())) {
                current.demoteFromMain();
                warehouseRepository.save(current);
            }
        });
    }

    private Long resolveBranchId(Long companyId, Long branchId) {
        if (branchId == null) {
            return null;
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new DomainException("La sucursal no existe"));
        if (!companyId.equals(branch.getCompanyId())) {
            throw new DomainException("La sucursal no pertenece a la misma compañía");
        }
        return branch.getId();
    }

    private Warehouse requireWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new DomainException("El almacén no existe"));
    }

    private void requireCompanyId(Long companyId) {
        if (companyId == null) {
            throw new DomainException("companyId es obligatorio");
        }
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCompanyId(),
                warehouse.getBranchId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getLocationAddress(),
                warehouse.isMain()
        );
    }
}
