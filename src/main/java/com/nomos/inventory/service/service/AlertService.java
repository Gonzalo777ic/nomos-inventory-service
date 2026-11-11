package com.nomos.inventory.service.service;

import com.nomos.inventory.service.model.AlertStatus;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import com.valeerp.inventory.stock.StockAlertDtos.AlertResponse;
import com.valeerp.inventory.stock.StockAlertDtos.EvaluationSummary;
import com.valeerp.inventory.stock.StockAlertService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fachada legada hacia {@link StockAlertService}. Sin delete físico.
 */
@Service
public class AlertService {

    private final StockAlertService stockAlertService;

    public AlertService(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
    }

    public List<StockAlertDTO> getStockAlertsCalculated() {
        return stockAlertService.calculateLowStockLegacy();
    }

    public List<AlertResponse> getAllAlerts(AlertStatus status) {
        return stockAlertService.list(status);
    }

    public List<AlertResponse> getAlertsByProduct(Long productId) {
        return stockAlertService.byProduct(productId);
    }

    public EvaluationSummary evaluate() {
        return stockAlertService.evaluateAndPersist();
    }

    public AlertResponse resolve(Long id) {
        return stockAlertService.resolve(id);
    }

    public AlertResponse dismiss(Long id) {
        return stockAlertService.dismiss(id);
    }
}
