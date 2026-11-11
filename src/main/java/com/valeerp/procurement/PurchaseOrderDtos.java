package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public final class PurchaseOrderDtos {

    private PurchaseOrderDtos() {
    }

    public record LineRequest(
            @NotNull Long productId,
            @NotNull @Positive Integer quantity,
            @NotNull Money unitCost,
            Integer leadTimeDays,
            LocalDate promisedDeliveryDate
    ) {
        public LineRequest(Long productId, Integer quantity, Money unitCost) {
            this(productId, quantity, unitCost, null, null);
        }
    }

    public record CreatePurchaseOrderRequest(
            @NotNull Long supplierId,
            @NotNull LocalDate orderDate,
            @NotNull LocalDate deliveryDate,
            @NotEmpty @Valid List<LineRequest> lines
    ) {
    }

    public record ReceiptLineRequest(
            @NotNull Long detailId,
            @NotNull @Positive Integer quantity
    ) {
    }

    public record ReceivePurchaseOrderRequest(
            @NotNull Long warehouseId,
            List<@Valid ReceiptLineRequest> lines
    ) {
    }

    public record PurchaseOrderLineResponse(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            Integer receivedQuantity,
            Integer residualQuantity,
            Money unitCost,
            Integer leadTimeDays,
            LocalDate promisedDeliveryDate
    ) {
    }

    public record PurchaseOrderResponse(
            Long id,
            Long supplierId,
            String supplierName,
            LocalDate orderDate,
            LocalDate deliveryDate,
            Money totalAmount,
            String status,
            Integer totalResidualQuantity,
            List<PurchaseOrderLineResponse> lines
    ) {
    }
}
