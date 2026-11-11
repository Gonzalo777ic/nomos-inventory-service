package com.valeerp.procurement;

import com.valeerp.procurement.PurchaseOrderDtos.CreatePurchaseOrderRequest;
import com.valeerp.procurement.PurchaseOrderDtos.PurchaseOrderResponse;
import com.valeerp.procurement.PurchaseOrderDtos.ReceivePurchaseOrderRequest;
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
@RequestMapping("/api/procurement/purchase-orders")
public class PurchaseOrderApiController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderApiController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public List<PurchaseOrderResponse> list() {
        return purchaseOrderService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return purchaseOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public PurchaseOrderResponse create(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        return purchaseOrderService.create(request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public PurchaseOrderResponse submit(@PathVariable Long id) {
        return purchaseOrderService.submit(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public PurchaseOrderResponse approve(@PathVariable Long id) {
        return purchaseOrderService.approve(id);
    }

    @PostMapping("/{id}/reject-approval")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public PurchaseOrderResponse rejectApproval(@PathVariable Long id) {
        return purchaseOrderService.rejectApproval(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public PurchaseOrderResponse cancel(@PathVariable Long id) {
        return purchaseOrderService.cancel(id);
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR')")
    public PurchaseOrderResponse receive(
            @PathVariable Long id,
            @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return purchaseOrderService.receive(id, request);
    }
}
