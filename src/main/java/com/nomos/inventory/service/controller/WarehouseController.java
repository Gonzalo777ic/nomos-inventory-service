package com.nomos.inventory.service.controller;

import com.valeerp.inventory.WarehouseDtos.CreateWarehouseRequest;
import com.valeerp.inventory.WarehouseDtos.UpdateWarehouseRequest;
import com.valeerp.inventory.WarehouseDtos.WarehouseResponse;
import com.valeerp.inventory.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public List<WarehouseResponse> list(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long branchId) {
        if (companyId != null && branchId != null) {
            return warehouseService.listByCompanyAndBranch(companyId, branchId);
        }
        if (companyId != null) {
            return warehouseService.listByCompany(companyId);
        }
        return warehouseService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public WarehouseResponse get(@PathVariable Long id) {
        return warehouseService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public WarehouseResponse create(@Valid @RequestBody CreateWarehouseRequest request) {
        return warehouseService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public WarehouseResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateWarehouseRequest request) {
        return warehouseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        warehouseService.retire(id);
    }
}
