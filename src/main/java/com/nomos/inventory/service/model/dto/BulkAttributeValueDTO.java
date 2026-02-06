package com.nomos.inventory.service.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkAttributeValueDTO {
    private List<Long> productIds;
    private Long attributeId;
    private String value;
}