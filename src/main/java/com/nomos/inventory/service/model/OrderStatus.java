package com.nomos.inventory.service.model;

public enum OrderStatus {

    BORRADOR("Borrador"),

    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"), 

    COMPLETO("Completo"),
    CANCELADO("Cancelado"); 

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}