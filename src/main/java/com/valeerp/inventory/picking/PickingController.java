package com.valeerp.inventory.picking;

import com.valeerp.inventory.picking.PickingDtos.AddPickingLineRequest;
import com.valeerp.inventory.picking.PickingDtos.CreatePickingRequest;
import com.valeerp.inventory.picking.PickingDtos.PickingResponse;
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
@RequestMapping("/api/inventory/pickings")
public class PickingController {

    private final PickingService pickingService;

    public PickingController(PickingService pickingService) {
        this.pickingService = pickingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<PickingResponse> list(@RequestParam Long companyId) {
        return pickingService.listByCompany(companyId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public PickingResponse get(@PathVariable Long id) {
        return pickingService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PickingResponse create(@Valid @RequestBody CreatePickingRequest request) {
        return pickingService.create(request);
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PickingResponse addLine(@PathVariable Long id, @Valid @RequestBody AddPickingLineRequest request) {
        return pickingService.addLine(id, request);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PickingResponse confirm(@PathVariable Long id) {
        return pickingService.confirm(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PickingResponse complete(@PathVariable Long id) {
        return pickingService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PickingResponse cancel(@PathVariable Long id) {
        return pickingService.cancel(id);
    }
}
