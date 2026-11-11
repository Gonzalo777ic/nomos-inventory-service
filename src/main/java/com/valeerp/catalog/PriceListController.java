package com.valeerp.catalog;

import com.valeerp.catalog.PriceListDtos.CreatePriceListRequest;
import com.valeerp.catalog.PriceListDtos.CreatePriceRuleRequest;
import com.valeerp.catalog.PriceListDtos.PriceListItemResponse;
import com.valeerp.catalog.PriceListDtos.PriceListResponse;
import com.valeerp.catalog.PriceListDtos.PriceRuleResponse;
import com.valeerp.catalog.PriceListDtos.ResolvePriceRequest;
import com.valeerp.catalog.PriceListDtos.ResolvedPriceResponse;
import com.valeerp.catalog.PriceListDtos.UpsertPriceListItemRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/price-lists")
public class PriceListController {

    private final PriceListService priceListService;

    public PriceListController(PriceListService priceListService) {
        this.priceListService = priceListService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<PriceListResponse> list() {
        return priceListService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public PriceListResponse get(@PathVariable Long id) {
        return priceListService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PriceListResponse create(@Valid @RequestBody CreatePriceListRequest request) {
        return priceListService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        priceListService.retire(id);
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<PriceListItemResponse> items(@PathVariable Long id) {
        return priceListService.listItems(id);
    }

    @PutMapping("/{id}/items")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PriceListItemResponse upsertItem(@PathVariable Long id,
                                            @Valid @RequestBody UpsertPriceListItemRequest request) {
        return priceListService.upsertItem(id, request);
    }

    @GetMapping("/{id}/rules")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public List<PriceRuleResponse> rules(@PathVariable Long id) {
        return priceListService.listRules(id);
    }

    @PostMapping("/{id}/rules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PriceRuleResponse createRule(@PathVariable Long id,
                                        @Valid @RequestBody CreatePriceRuleRequest request) {
        return priceListService.createRule(id, request);
    }

    @DeleteMapping("/{id}/rules/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retireRule(@PathVariable Long id, @PathVariable Long ruleId) {
        priceListService.retireRule(id, ruleId);
    }

    @PostMapping("/resolve")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public ResolvedPriceResponse resolve(@Valid @RequestBody ResolvePriceRequest request) {
        return priceListService.resolve(request);
    }
}
