package com.nomos.inventory.service.model;

public enum OrderStatus {

    BORRADOR("Borrador"),
    PENDIENTE_APROBACION("Pendiente de aprobación"),
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    PARCIAL("Recepción parcial"),
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