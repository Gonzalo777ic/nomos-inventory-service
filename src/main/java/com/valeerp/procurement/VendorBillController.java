package com.valeerp.procurement;

import com.valeerp.procurement.VendorBillDtos.CreateVendorBillRequest;
import com.valeerp.procurement.VendorBillDtos.VendorBillResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/vendor-bills")
public class VendorBillController {

    private final VendorBillService vendorBillService;

    public VendorBillController(VendorBillService vendorBillService) {
        this.vendorBillService = vendorBillService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR')")
    public List<VendorBillResponse> list() {
        return vendorBillService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR')")
    public VendorBillResponse get(@PathVariable Long id) {
        return vendorBillService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public VendorBillResponse create(@Valid @RequestBody CreateVendorBillRequest request) {
        return vendorBillService.create(request);
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public VendorBillResponse post(@PathVariable Long id) {
        return vendorBillService.post(id);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public VendorBillResponse pay(@PathVariable Long id) {
        return vendorBillService.markPaid(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public VendorBillResponse cancel(@PathVariable Long id) {
        return vendorBillService.cancel(id);
    }
}
