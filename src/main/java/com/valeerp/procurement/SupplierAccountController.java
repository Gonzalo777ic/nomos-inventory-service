package com.valeerp.procurement;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/procurement/supplier-accounts")
public class SupplierAccountController {

    private final SupplierAccountService supplierAccountService;

    public SupplierAccountController(SupplierAccountService supplierAccountService) {
        this.supplierAccountService = supplierAccountService;
    }

    @GetMapping
    public List<SupplierAccount> list() {
        return supplierAccountService.findAll();
    }

    @GetMapping("/me")
    public SupplierLink me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long supplierId = supplierAccountService.supplierIdOf(authentication.getName())
                .orElse(null);
        return new SupplierLink(null, authentication.getName(), supplierId);
    }

    @PutMapping
    public SupplierAccount link(@RequestBody SupplierLink link) {
        return supplierAccountService.link(link.userId(), link.username(), link.supplierId());
    }

    public record SupplierLink(Long userId, String username, Long supplierId) {
    }
}
