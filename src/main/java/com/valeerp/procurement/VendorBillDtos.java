package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public final class VendorBillDtos {

    private VendorBillDtos() {
    }

    public record LineRequest(
            Long productId,
            @NotBlank String description,
            @NotNull @Positive Integer quantity,
            @NotNull Money unitPrice
    ) {
    }

    public record CreateVendorBillRequest(
            @NotNull Long supplierId,
            Long purchaseOrderId,
            @NotBlank String documentNumber,
            @NotNull LocalDate billDate,
            @NotEmpty @Valid List<LineRequest> lines,
            Money taxAmount
    ) {
    }

    public record VendorBillLineResponse(
            Long id,
            Long productId,
            String description,
            Integer quantity,
            Money unitPrice,
            Money lineTotal
    ) {
    }

    public record VendorBillResponse(
            Long id,
            Long supplierId,
            Long purchaseOrderId,
            String documentNumber,
            LocalDate billDate,
            String status,
            Money untaxedAmount,
            Money taxAmount,
            Money totalAmount,
            String accountingStubRef,
            List<VendorBillLineResponse> lines
    ) {
    }
}
