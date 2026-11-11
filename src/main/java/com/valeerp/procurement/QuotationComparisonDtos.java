package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class QuotationComparisonDtos {

    private QuotationComparisonDtos() {
    }

    public record CompareQuotationsRequest(
            @NotEmpty
            @Size(min = 2, message = "Se requieren al menos dos cotizaciones")
            List<Long> quotationIds
    ) {
    }

    public record OfferResponse(
            Long quotationId,
            Long supplierId,
            String supplierName,
            Integer quantity,
            Money unitPrice
    ) {
    }

    public record ProductComparisonResponse(
            Long productId,
            String productName,
            List<OfferResponse> offers,
            Long bestQuotationId,
            Long bestSupplierId,
            Money bestUnitPrice
    ) {
    }

    public record ComparisonResponse(
            List<Long> quotationIds,
            List<ProductComparisonResponse> products
    ) {
    }
}
