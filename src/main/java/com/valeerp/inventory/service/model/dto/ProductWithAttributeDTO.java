package com.valeerp.inventory.service.model.dto;

public interface ProductWithAttributeDTO {
    Long getProductId();
    String getProductName();
    String getProductSku();
    String getAttributeValue();
}