package com.nomos.inventory.service.controller;

import com.valeerp.inventory.QuantQueryService;
import com.valeerp.inventory.StockDtos.OpenQuantRequest;
import com.valeerp.inventory.StockDtos.QuantResponse;
import com.valeerp.inventory.StockDtos.UpdateQuantRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

/**
 * Quants (lotes/series). No hay PUT de cantidad: solo metadatos; la cantidad cambia vía movimientos.
 */
@RestController
@RequestMapping("/api/inventory/items")
public class InventoryItemController {

    private final QuantQueryService quantQueryService;

    public InventoryItemController(QuantQueryService quantQueryService) {
        this.quantQueryService = quantQueryService;
    }

    @GetMapping("/product/{productId}")
    public List<QuantResponse> getItemsByProduct(@PathVariable Long productId) {
        return quantQueryService.listByProduct(productId);
    }

    @GetMapping("/product/{productId}/total-stock")
    public Integer getTotalStockByProduct(@PathVariable Long productId) {
        return quantQueryService.totalStock(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuantResponse createItem(@Valid @RequestBody OpenQuantRequest request) {
        return quantQueryService.openEmpty(request);
    }

    @PutMapping("/{id}")
    public QuantResponse updateItem(@PathVariable Long id, @Valid @RequestBody UpdateQuantRequest request) {
        return quantQueryService.updateMetadata(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        quantQueryService.retireEmpty(id);
    }
}
