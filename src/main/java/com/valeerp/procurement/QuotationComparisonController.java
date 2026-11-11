package com.valeerp.procurement;

import com.valeerp.procurement.QuotationComparisonDtos.CompareQuotationsRequest;
import com.valeerp.procurement.QuotationComparisonDtos.ComparisonResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procurement/quotations")
public class QuotationComparisonController {

    private final QuotationComparisonService comparisonService;

    public QuotationComparisonController(QuotationComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @PostMapping("/compare")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS', 'ROLE_VENDOR')")
    public ComparisonResponse compare(@Valid @RequestBody CompareQuotationsRequest request) {
        return comparisonService.compare(request);
    }
}
