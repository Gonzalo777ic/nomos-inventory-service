package com.valeerp.procurement;

import com.valeerp.inventory.service.model.QuotationStatus;
import com.valeerp.kernel.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public final class QuotationDtos {

    private QuotationDtos() {
    }

    public record LineRequest(
            Long productId,
            String productName,
            String skuSuggestion,
            @NotNull @Positive Integer quantity,
            Money quotedPrice,
            Integer leadTimeDays,
            LocalDate promisedDeliveryDate
    ) {
        public LineRequest(Long productId, String productName, String skuSuggestion,
                           Integer quantity, Money quotedPrice) {
            this(productId, productName, skuSuggestion, quantity, quotedPrice, null, null);
        }
    }

    public record CreateRequest(
            @NotNull Long supplierId,
            LocalDate requestDate,
            LocalDate expirationDate,
            String notes,
            @Valid List<LineRequest> lines
    ) {
    }

    public record UpdateDraftRequest(
            String notes,
            LocalDate expirationDate,
            @Valid List<LineRequest> lines
    ) {
    }

    public record LineResponse(
            Long id,
            Long productId,
            String productName,
            String skuSuggestion,
            Integer quantity,
            Money quotedPrice,
            Integer leadTimeDays,
            LocalDate promisedDeliveryDate
    ) {
    }

    public record QuotationResponse(
            Long id,
            Long supplierId,
            String supplierName,
            LocalDate requestDate,
            LocalDate expirationDate,
            QuotationStatus status,
            Money totalEstimated,
            String notes,
            List<LineResponse> lines
    ) {
    }

    public record ConvertResponse(
            Long quotationId,
            Long purchaseOrderId,
            QuotationStatus quotationStatus
    ) {
    }
}
