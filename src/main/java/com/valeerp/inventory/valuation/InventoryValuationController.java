package com.valeerp.inventory.valuation;

import com.valeerp.inventory.valuation.InventoryValuationStub.ValuationSnapshot;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de lectura del stub de valoración. Sin publicación contable.
 */
@RestController
@RequestMapping("/api/inventory/valuation")
public class InventoryValuationController {

    private final InventoryValuationStub valuationStub;

    public InventoryValuationController(InventoryValuationStub valuationStub) {
        this.valuationStub = valuationStub;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AUDITOR', 'ROLE_VENDOR')")
    public ValuationSnapshot snapshot(
            @RequestParam(defaultValue = "PERIODIC") InventoryValuationMode mode,
            @RequestParam(required = false) Long warehouseId) {
        return valuationStub.snapshot(mode, warehouseId);
    }
}
