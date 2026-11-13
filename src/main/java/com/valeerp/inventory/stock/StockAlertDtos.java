package com.valeerp.inventory.stock;

import com.valeerp.inventory.service.model.AlertStatus;
import com.valeerp.inventory.service.model.AlertType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class StockAlertDtos {

    private StockAlertDtos() {
    }

    public record AlertResponse(
            Long id,
            Long productId,
            String productSku,
            String productName,
            Long inventoryItemId,
            String lotNumber,
            AlertType type,
            AlertStatus status,
            String title,
            String description,
            Integer severity,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt
    ) {
    }

    public record LowStockSnapshot(
            Long productId,
            String productName,
            String sku,
            int currentStock,
            int minStockThreshold,
            int deficit,
            String level
    ) {
    }

    public record ExpirySnapshot(
            Long inventoryItemId,
            Long productId,
            String productSku,
            String lotNumber,
            LocalDate expirationDate,
            String level
    ) {
    }

    public record EvaluationSummary(
            int lowStockOpened,
            int expiryOpened,
            int resolved
    ) {
    }
}
