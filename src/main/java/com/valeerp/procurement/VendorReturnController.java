package com.valeerp.procurement;

import com.valeerp.procurement.VendorReturnDtos.CreateRequest;
import com.valeerp.procurement.VendorReturnDtos.Response;
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
@RequestMapping("/api/procurement/vendor-returns")
public class VendorReturnController {

    private final VendorReturnService vendorReturnService;

    public VendorReturnController(VendorReturnService vendorReturnService) {
        this.vendorReturnService = vendorReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_AUDITOR')")
    public List<Response> list(@RequestParam Long purchaseOrderId) {
        return vendorReturnService.listByPurchaseOrder(purchaseOrderId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_AUDITOR')")
    public Response get(@PathVariable Long id) {
        return vendorReturnService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response create(@Valid @RequestBody CreateRequest request) {
        return vendorReturnService.createDraft(request);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response confirm(@PathVariable Long id) {
        return vendorReturnService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response cancel(@PathVariable Long id) {
        return vendorReturnService.cancel(id);
    }
}
