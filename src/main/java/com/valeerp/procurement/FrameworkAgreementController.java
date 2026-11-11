package com.valeerp.procurement;

import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.FrameworkAgreementDtos.CreateRequest;
import com.valeerp.procurement.FrameworkAgreementDtos.PriceLookupResponse;
import com.valeerp.procurement.FrameworkAgreementDtos.Response;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/procurement/framework-agreements")
public class FrameworkAgreementController {

    private final FrameworkAgreementService frameworkAgreementService;

    public FrameworkAgreementController(FrameworkAgreementService frameworkAgreementService) {
        this.frameworkAgreementService = frameworkAgreementService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_AUDITOR')")
    public List<Response> list(@RequestParam Long supplierId) {
        return frameworkAgreementService.listBySupplier(supplierId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_AUDITOR')")
    public Response get(@PathVariable Long id) {
        return frameworkAgreementService.get(id);
    }

    @GetMapping("/price")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_AUDITOR')")
    public PriceLookupResponse price(
            @RequestParam Long supplierId,
            @RequestParam Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate onDate) {
        return frameworkAgreementService.lookupPrice(supplierId, productId, onDate)
                .orElseThrow(() -> new DomainException("No hay precio vigente en acuerdo marco"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response create(@Valid @RequestBody CreateRequest request) {
        return frameworkAgreementService.create(request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response activate(@PathVariable Long id) {
        return frameworkAgreementService.activate(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public Response close(@PathVariable Long id) {
        return frameworkAgreementService.close(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        frameworkAgreementService.retire(id);
    }
}
