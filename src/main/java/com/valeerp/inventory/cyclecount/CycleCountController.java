package com.valeerp.inventory.cyclecount;

import com.valeerp.inventory.cyclecount.CycleCountDtos.CreateCycleCountRequest;
import com.valeerp.inventory.cyclecount.CycleCountDtos.CycleCountResponse;
import com.valeerp.inventory.cyclecount.CycleCountDtos.RecordCountRequest;
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
@RequestMapping("/api/inventory/cycle-counts")
public class CycleCountController {

    private final CycleCountService cycleCountService;

    public CycleCountController(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<CycleCountResponse> list(@RequestParam Long companyId) {
        return cycleCountService.listByCompany(companyId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public CycleCountResponse get(@PathVariable Long id) {
        return cycleCountService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public CycleCountResponse create(@Valid @RequestBody CreateCycleCountRequest request) {
        return cycleCountService.create(request);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public CycleCountResponse start(@PathVariable Long id) {
        return cycleCountService.start(id);
    }

    @PostMapping("/{id}/lines/{lineId}/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public CycleCountResponse recordCount(
            @PathVariable Long id,
            @PathVariable Long lineId,
            @Valid @RequestBody RecordCountRequest request) {
        return cycleCountService.recordLineCount(id, lineId, request);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public CycleCountResponse complete(@PathVariable Long id) {
        return cycleCountService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public CycleCountResponse cancel(@PathVariable Long id) {
        return cycleCountService.cancel(id);
    }
}
