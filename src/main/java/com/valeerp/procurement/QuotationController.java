package com.valeerp.procurement;

import com.valeerp.procurement.QuotationDtos.ConvertResponse;
import com.valeerp.procurement.QuotationDtos.CreateRequest;
import com.valeerp.procurement.QuotationDtos.QuotationResponse;
import com.valeerp.procurement.QuotationDtos.UpdateDraftRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @GetMapping
    public List<QuotationResponse> list(@RequestParam(required = false) Long supplierId) {
        return quotationService.list(supplierId);
    }

    @GetMapping("/{id}")
    public QuotationResponse get(@PathVariable Long id) {
        return quotationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuotationResponse create(@Valid @RequestBody CreateRequest request) {
        return quotationService.create(request);
    }

    @PutMapping("/{id}")
    public QuotationResponse updateDraft(@PathVariable Long id, @Valid @RequestBody UpdateDraftRequest request) {
        return quotationService.updateDraft(id, request);
    }

    @PostMapping("/{id}/send")
    public QuotationResponse send(@PathVariable Long id) {
        return quotationService.send(id);
    }

    @PostMapping("/{id}/cancel")
    public QuotationResponse cancel(@PathVariable Long id) {
        return quotationService.cancel(id);
    }

    @PostMapping("/{id}/respond")
    public QuotationResponse respond(@PathVariable Long id) {
        return quotationService.markResponded(id);
    }

    @PostMapping("/{id}/accept")
    public QuotationResponse accept(@PathVariable Long id) {
        return quotationService.accept(id);
    }

    @PostMapping("/{id}/reject")
    public QuotationResponse reject(@PathVariable Long id) {
        return quotationService.reject(id);
    }

    @PutMapping("/{id}/details/{detailId}/link-product/{productId}")
    public QuotationResponse linkProduct(@PathVariable Long id,
                                         @PathVariable Long detailId,
                                         @PathVariable Long productId) {
        return quotationService.linkProduct(id, detailId, productId);
    }

    @PostMapping("/{id}/convert-to-order")
    public ConvertResponse convert(@PathVariable Long id) {
        return quotationService.convert(id);
    }
}
