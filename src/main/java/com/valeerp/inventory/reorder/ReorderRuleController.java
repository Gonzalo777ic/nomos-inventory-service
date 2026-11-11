package com.valeerp.inventory.reorder;

import com.valeerp.inventory.reorder.ReorderRuleDtos.CreateReorderRuleRequest;
import com.valeerp.inventory.reorder.ReorderRuleDtos.ReplenishmentSuggestionResponse;
import com.valeerp.inventory.reorder.ReorderRuleDtos.ReorderRuleResponse;
import com.valeerp.inventory.reorder.ReorderRuleDtos.UpdateReorderRuleRequest;
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
@RequestMapping("/api/inventory/reorder-rules")
public class ReorderRuleController {

    private final ReorderRuleService reorderRuleService;

    public ReorderRuleController(ReorderRuleService reorderRuleService) {
        this.reorderRuleService = reorderRuleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<ReorderRuleResponse> list(@RequestParam Long warehouseId) {
        return reorderRuleService.listByWarehouse(warehouseId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public ReorderRuleResponse get(@PathVariable Long id) {
        return reorderRuleService.get(id);
    }

    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<ReplenishmentSuggestionResponse> suggestions(@RequestParam Long warehouseId) {
        return reorderRuleService.suggestionsForWarehouse(warehouseId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ReorderRuleResponse create(@Valid @RequestBody CreateReorderRuleRequest request) {
        return reorderRuleService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ReorderRuleResponse update(@PathVariable Long id, @Valid @RequestBody UpdateReorderRuleRequest request) {
        return reorderRuleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        reorderRuleService.retire(id);
    }
}
