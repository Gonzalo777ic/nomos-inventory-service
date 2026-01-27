package com.nomos.inventory.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertDTO {
    private Long productId;
    private String productName;
    private String sku;
    private String imageUrl;
    private Integer currentStock;
    private Integer minStockThreshold;
    private Integer deficit; // Cuánto falta para llegar al mínimo (opcional)
    private String status; // "CRITICAL", "LOW"
}