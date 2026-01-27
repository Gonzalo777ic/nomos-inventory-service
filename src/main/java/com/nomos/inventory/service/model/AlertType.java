package com.nomos.inventory.service.model;

public enum AlertType {

    LOW_STOCK("Stock Bajo"),
    CRITICAL_STOCK("Stock Crítico"),
    NEAR_EXPIRATION("Próximo a Vencer"),
    EXPIRED("Vencido");
    private final String displayValue;

    AlertType(String displayValue) {
        this.displayValue = displayValue;
    }
}
