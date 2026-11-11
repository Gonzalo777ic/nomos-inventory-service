package com.valeerp.inventory.stock;

import com.nomos.inventory.service.model.AlertStatus;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import com.valeerp.inventory.stock.StockAlertDtos.AlertResponse;
import com.valeerp.inventory.stock.StockAlertDtos.EvaluationSummary;
import com.valeerp.inventory.stock.StockAlertDtos.ExpirySnapshot;
import com.valeerp.inventory.stock.StockAlertDtos.LowStockSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/alerts")
public class StockAlertController {

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
    }

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) AlertStatus status) {
        return stockAlertService.list(status);
    }

    @GetMapping("/product/{productId}")
    public List<AlertResponse> byProduct(@PathVariable Long productId) {
        return stockAlertService.byProduct(productId);
    }

    @GetMapping("/low-stock")
    public List<LowStockSnapshot> lowStock() {
        return stockAlertService.calculateLowStock();
    }

    @GetMapping("/low-stock/legacy")
    public List<StockAlertDTO> lowStockLegacy() {
        return stockAlertService.calculateLowStockLegacy();
    }

    @GetMapping("/expiry")
    public List<ExpirySnapshot> expiry() {
        return stockAlertService.calculateExpiryAlerts();
    }

    @PostMapping("/evaluate")
    public EvaluationSummary evaluate() {
        return stockAlertService.evaluateAndPersist();
    }

    @PostMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable Long id) {
        return stockAlertService.resolve(id);
    }

    @PostMapping("/{id}/dismiss")
    public AlertResponse dismiss(@PathVariable Long id) {
        return stockAlertService.dismiss(id);
    }
}
