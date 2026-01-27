package com.nomos.inventory.service.service;

import com.nomos.inventory.service.model.Alert;
import com.nomos.inventory.service.model.AlertStatus;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import com.nomos.inventory.service.repository.AlertRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final ProductRepository productRepository;
    private final AlertRepository alertRepository;

    /**
     * Obtiene una lista de productos que están por debajo de su umbral de stock mínimo.
     * Esta información se calcula en tiempo real consultando el stock actual.
     */
    public List<StockAlertDTO> getStockAlertsCalculated() {

        List<StockAlertDTO> alerts = productRepository.findProductsWithLowStock();


        alerts.forEach(alert -> {
            if (alert.getCurrentStock() <= 0) {
                alert.setStatus("CRITICAL");
            } else {
                alert.setStatus("LOW");
            }
        });

        return alerts;
    }




    /**
     * Obtiene todas las alertas registradas en la base de datos.
     * @param status (Opcional) Filtrar por estado (ACTIVE, RESOLVED, DISMISSED)
     */
    public List<Alert> getAllAlerts(AlertStatus status) {
        if (status != null) {

            return alertRepository.findByStatusOrderBySeverityAscCreatedAtDesc(status);
        }

        return alertRepository.findAll();
    }

    /**
     * Obtiene el historial de alertas de un producto específico.
     */
    public List<Alert> getAlertsByProduct(Long productId) {
        return alertRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    /**
     * Crea una nueva alerta manualmente o desde un proceso automático.
     */
    @Transactional
    public Alert createAlert(Alert alert) {


        boolean exists = alertRepository.existsByProductIdAndTypeAndStatus(
                alert.getProduct().getId(),
                alert.getType(),
                AlertStatus.ACTIVE
        );

        if (exists) {


            throw new IllegalArgumentException("Ya existe una alerta activa de este tipo para el producto.");
        }

        return alertRepository.save(alert);
    }

}