package com.nomos.inventory.service.model;

public enum OrderStatus {
    PENDIENTE("Pendiente"),         // Creada pero aún no recibida
    RECIBIDO_PARCIAL("Recibido Parcial"), // Parte de la mercancía ha llegado
    COMPLETO("Completo"),           // Toda la mercancía ha sido recibida
    CANCELADO("Cancelado");         // Orden anulada

    private final String displayValue;

    OrderStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
