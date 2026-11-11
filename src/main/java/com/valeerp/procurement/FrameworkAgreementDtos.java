package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class FrameworkAgreementDtos {

    private FrameworkAgreementDtos() {
    }

    public record LineRequest(
            @NotNull Long productId,
            @NotNull Money unitPrice,
            LocalDate priceValidFrom,
            LocalDate priceValidTo
    ) {
    }

    public record CreateRequest(
            @NotBlank String code,
            @NotNull Long supplierId,
            @NotNull LocalDate validFrom,
            @NotNull LocalDate validTo,
            String notes,
            @NotEmpty @Valid List<LineRequest> lines
    ) {
    }

    public record LineResponse(
            Long id,
            Long productId,
            Money unitPrice,
            LocalDate priceValidFrom,
            LocalDate priceValidTo
    ) {
    }

    public record Response(
            Long id,
            String code,
            Long supplierId,
            LocalDate validFrom,
            LocalDate validTo,
            FrameworkAgreementStatus status,
            String notes,
            List<LineResponse> lines
    ) {
    }

    public record PriceLookupResponse(
            Long agreementId,
            String agreementCode,
            Long productId,
            LocalDate onDate,
            Money unitPrice
    ) {
    }
}
