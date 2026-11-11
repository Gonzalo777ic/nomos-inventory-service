package com.valeerp.mrp;

import com.valeerp.mrp.ManufacturingOrderDtos.CreateRequest;
import com.valeerp.mrp.ManufacturingOrderDtos.Response;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mrp/manufacturing-orders")
public class ManufacturingOrderController {

    private final ManufacturingOrderService manufacturingOrderService;

    public ManufacturingOrderController(ManufacturingOrderService manufacturingOrderService) {
        this.manufacturingOrderService = manufacturingOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<Response> list(@RequestParam Long warehouseId) {
        return manufacturingOrderService.listByWarehouse(warehouseId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public Response get(@PathVariable Long id) {
        return manufacturingOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response create(@Valid @RequestBody CreateRequest request) {
        return manufacturingOrderService.create(request);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response confirm(@PathVariable Long id) {
        return manufacturingOrderService.confirm(id);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response start(@PathVariable Long id) {
        return manufacturingOrderService.start(id);
    }

    @PostMapping("/{id}/consume")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response consume(@PathVariable Long id) {
        return manufacturingOrderService.consume(id);
    }

    @PostMapping("/{id}/produce")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response produce(@PathVariable Long id) {
        return manufacturingOrderService.produce(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response cancel(@PathVariable Long id) {
        return manufacturingOrderService.cancel(id);
    }
}
