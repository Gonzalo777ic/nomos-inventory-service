package com.nomos.inventory.service.model;

public enum QuotationStatus {
    BORRADOR("Borrador"),           
    ENVIADO("Enviado"),             
    CANCELADO("Cancelado"),
    RESPONDIDO("Respondido"),       
    APROBADO("Aprobado"),           
    CONVERTIDO("Convertido a OC"),  
    RECHAZADO("Rechazado");         

    private final String displayValue;

    QuotationStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}