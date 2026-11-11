package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public final class VendorReturnDtos {

    private VendorReturnDtos() {
    }

    public record LineRequest(
            @NotNull Long productId,
            @NotNull Long lotId,
            @NotNull @Positive Integer quantity,
            @NotNull Money unitCost
    ) {
    }

    public record CreateRequest(
            @NotNull Long purchaseOrderId,
            LocalDate returnDate,
            String reason,
            @NotEmpty @Valid List<LineRequest> lines
    ) {
    }

    public record LineResponse(
            Long id,
            Long productId,
            Long lotId,
            Integer quantity,
            Money unitCost
    ) {
    }

    public record Response(
            Long id,
            Long purchaseOrderId,
            Long supplierId,
            LocalDate returnDate,
            VendorReturnStatus status,
            Money totalAmount,
            String reason,
            List<LineResponse> lines
    ) {
    }
}
