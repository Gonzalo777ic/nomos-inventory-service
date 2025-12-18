package com.nomos.inventory.service.model;

public enum OrderStatus {
    PENDIENTE("Pendiente"),         
    CONFIRMADO("Confirmado"),         
    RECIBIDO_PARCIAL("Recibido Parcial"), 
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
