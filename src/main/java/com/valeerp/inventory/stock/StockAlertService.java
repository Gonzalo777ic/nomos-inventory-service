package com.valeerp.inventory.stock;

import com.valeerp.inventory.service.model.Alert;
import com.valeerp.inventory.service.model.AlertStatus;
import com.valeerp.inventory.service.model.AlertType;
import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.dto.StockAlertDTO;
import com.valeerp.inventory.service.repository.AlertRepository;
import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.stock.StockAlertDtos.AlertResponse;
import com.valeerp.inventory.stock.StockAlertDtos.EvaluationSummary;
import com.valeerp.inventory.stock.StockAlertDtos.ExpirySnapshot;
import com.valeerp.inventory.stock.StockAlertDtos.LowStockSnapshot;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Alertas de stock bajo y caducidad. Umbrales: {@link Product#getMinStockThreshold()}
 * y {@link StockQuantityPolicy#nearExpiryDays()} — nunca literales en este servicio.
 */
@Service
public class StockAlertService {

    private static final int SEVERITY_CRITICAL = 1;
    private static final int SEVERITY_LOW = 2;
    private static final int SEVERITY_EXPIRED = 1;
    private static final int SEVERITY_NEAR = 3;

    private final ProductRepository productRepository;
    private final InventoryItemRepository itemRepository;
    private final AlertRepository alertRepository;
    private final StockQuantityPolicy quantityPolicy;
    private final Clock clock;

    public StockAlertService(ProductRepository productRepository,
                             InventoryItemRepository itemRepository,
                             AlertRepository alertRepository,
                             StockQuantityPolicy quantityPolicy,
                             Clock clock) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.alertRepository = alertRepository;
        this.quantityPolicy = quantityPolicy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LowStockSnapshot> calculateLowStock() {
        List<LowStockSnapshot> result = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            Integer threshold = product.getMinStockThreshold();
            if (threshold == null) {
                continue;
            }
            int onHand = onHandOf(product.getId());
            if (!quantityPolicy.isBelowProductThreshold(onHand, threshold)) {
                continue;
            }
            String level = quantityPolicy.isCritical(onHand) ? "CRITICAL" : "LOW";
            result.add(new LowStockSnapshot(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    onHand,
                    threshold,
                    threshold - onHand,
                    level
            ));
        }
        return result;
    }

    /** Compatibilidad con consumidores del DTO legado. */
    @Transactional(readOnly = true)
    public List<StockAlertDTO> calculateLowStockLegacy() {
        return calculateLowStock().stream()
                .map(s -> StockAlertDTO.builder()
                        .productId(s.productId())
                        .productName(s.productName())
                        .sku(s.sku())
                        .currentStock(s.currentStock())
                        .minStockThreshold(s.minStockThreshold())
                        .deficit(s.deficit())
                        .status(s.level())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpirySnapshot> calculateExpiryAlerts() {
        LocalDate today = LocalDate.now(clock);
        List<ExpirySnapshot> result = new ArrayList<>();
        for (InventoryItem item : itemRepository.findAll()) {
            if (item.getExpirationDate() == null) {
                continue;
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            String level = null;
            if (quantityPolicy.isExpired(item.getExpirationDate(), today)) {
                level = "EXPIRED";
            } else if (quantityPolicy.isNearExpiry(item.getExpirationDate(), today)) {
                level = "NEAR_EXPIRATION";
            }
            if (level == null) {
                continue;
            }
            result.add(new ExpirySnapshot(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getSku(),
                    item.getLotNumber(),
                    item.getExpirationDate(),
                    level
            ));
        }
        return result;
    }

    @Transactional
    public EvaluationSummary evaluateAndPersist() {
        LocalDateTime now = LocalDateTime.now(clock);
        int openedLow = 0;
        int openedExpiry = 0;
        int resolved = 0;

        for (LowStockSnapshot snapshot : calculateLowStock()) {
            Product product = productRepository.findById(snapshot.productId()).orElseThrow();
            AlertType type = "CRITICAL".equals(snapshot.level())
                    ? AlertType.CRITICAL_STOCK
                    : AlertType.LOW_STOCK;
            if (!alertRepository.existsByProductIdAndTypeAndStatus(product.getId(), type, AlertStatus.ACTIVE)) {
                alertRepository.save(Alert.open(
                        product,
                        null,
                        type,
                        type.getDisplayValue() + ": " + product.getSku(),
                        "Stock " + snapshot.currentStock() + " / umbral " + snapshot.minStockThreshold(),
                        "CRITICAL".equals(snapshot.level()) ? SEVERITY_CRITICAL : SEVERITY_LOW,
                        now
                ));
                openedLow++;
            }
        }

        for (ExpirySnapshot snapshot : calculateExpiryAlerts()) {
            InventoryItem item = itemRepository.findById(snapshot.inventoryItemId()).orElseThrow();
            AlertType type = "EXPIRED".equals(snapshot.level()) ? AlertType.EXPIRED : AlertType.NEAR_EXPIRATION;
            if (!alertRepository.existsByInventoryItemIdAndTypeAndStatus(item.getId(), type, AlertStatus.ACTIVE)) {
                alertRepository.save(Alert.open(
                        item.getProduct(),
                        item,
                        type,
                        type.getDisplayValue() + ": " + snapshot.lotNumber(),
                        "Caduca " + snapshot.expirationDate()
                                + " (horizonte " + quantityPolicy.nearExpiryDays() + " días)",
                        "EXPIRED".equals(snapshot.level()) ? SEVERITY_EXPIRED : SEVERITY_NEAR,
                        now
                ));
                openedExpiry++;
            }
        }

        for (Alert active : alertRepository.findByStatusOrderBySeverityAscCreatedAtDesc(AlertStatus.ACTIVE)) {
            if (shouldAutoResolve(active)) {
                active.resolve(now);
                alertRepository.save(active);
                resolved++;
            }
        }
        return new EvaluationSummary(openedLow, openedExpiry, resolved);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> list(AlertStatus status) {
        List<Alert> alerts = status == null
                ? alertRepository.findAll()
                : alertRepository.findByStatusOrderBySeverityAscCreatedAtDesc(status);
        return alerts.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> byProduct(Long productId) {
        return alertRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AlertResponse resolve(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new DomainException("Alerta no encontrada"));
        alert.resolve(LocalDateTime.now(clock));
        return toResponse(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponse dismiss(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new DomainException("Alerta no encontrada"));
        alert.dismiss(LocalDateTime.now(clock));
        return toResponse(alertRepository.save(alert));
    }

    private boolean shouldAutoResolve(Alert alert) {
        if (alert.getType() == AlertType.LOW_STOCK || alert.getType() == AlertType.CRITICAL_STOCK) {
            int onHand = onHandOf(alert.getProduct().getId());
            return !quantityPolicy.isBelowProductThreshold(onHand, alert.getProduct().getMinStockThreshold());
        }
        if (alert.getInventoryItem() == null) {
            return false;
        }
        InventoryItem item = alert.getInventoryItem();
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return true;
        }
        LocalDate today = LocalDate.now(clock);
        if (alert.getType() == AlertType.EXPIRED) {
            return !quantityPolicy.isExpired(item.getExpirationDate(), today);
        }
        if (alert.getType() == AlertType.NEAR_EXPIRATION) {
            return !quantityPolicy.isNearExpiry(item.getExpirationDate(), today)
                    && !quantityPolicy.isExpired(item.getExpirationDate(), today);
        }
        return false;
    }

    private int onHandOf(Long productId) {
        Integer total = itemRepository.calculateTotalStockByProductId(productId);
        return total == null ? 0 : total;
    }

    private AlertResponse toResponse(Alert alert) {
        InventoryItem item = alert.getInventoryItem();
        return new AlertResponse(
                alert.getId(),
                alert.getProduct().getId(),
                alert.getProduct().getSku(),
                alert.getProduct().getName(),
                item == null ? null : item.getId(),
                item == null ? null : item.getLotNumber(),
                alert.getType(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getSeverity(),
                alert.getCreatedAt(),
                alert.getResolvedAt()
        );
    }
}
