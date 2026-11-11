package com.valeerp.mrp;

import com.valeerp.mrp.BomDtos.CreateRequest;
import com.valeerp.mrp.BomDtos.ExplosionRequest;
import com.valeerp.mrp.BomDtos.ExplosionResponse;
import com.valeerp.mrp.BomDtos.Response;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/api/mrp/boms")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public List<Response> list(@RequestParam Long productId) {
        return bomService.listByProduct(productId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public Response get(@PathVariable Long id) {
        return bomService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response create(@Valid @RequestBody CreateRequest request) {
        return bomService.create(request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response activate(@PathVariable Long id) {
        return bomService.activate(id);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR')")
    public Response archive(@PathVariable Long id) {
        return bomService.archive(id);
    }

    @PostMapping("/{id}/explode")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_AUDITOR')")
    public ExplosionResponse explode(@PathVariable Long id, @Valid @RequestBody ExplosionRequest request) {
        return bomService.explode(id, request.finishedQuantity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void retire(@PathVariable Long id) {
        bomService.retire(id);
    }
}
