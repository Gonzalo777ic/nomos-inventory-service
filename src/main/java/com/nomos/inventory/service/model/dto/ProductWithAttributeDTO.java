package com.nomos.inventory.service.model.dto;

public interface ProductWithAttributeDTO {
    Long getProductId();
    String getProductName();
    String getProductSku();
    String getAttributeValue();
}