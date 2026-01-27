package com.nomos.inventory.service.model;

public enum AlertStatus {
    ACTIVE("Activa"),
    DISMISSED("Ignorada"),
    RESOLVED("Resuelta");

    private final String displayValue;

    AlertStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}