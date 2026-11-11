package com.valeerp.inventory;

import com.valeerp.kernel.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class StockDtos {

    private StockDtos() {
    }

    public record QuantResponse(
            Long id,
            Long productId,
            String productSku,
            Long warehouseId,
            Long internalLocationId,
            String lotNumber,
            String serialNumber,
            Integer quantity,
            Integer reservedQuantity,
            Integer availableQuantity,
            Money unitCost,
            LocalDate expirationDate,
            LocalDateTime entryDate
    ) {
    }

    public record OpenQuantRequest(
            @NotNull Long productId,
            @NotNull Long warehouseId,
            Long internalLocationId,
            @NotBlank String lotNumber,
            String serialNumber,
            Money unitCost,
            LocalDate expirationDate
    ) {
    }

    public record UpdateQuantRequest(
            Money unitCost,
            LocalDate expirationDate
    ) {
    }

    public record MovementResponse(
            Long id,
            Long quantId,
            Long productId,
            Long warehouseId,
            Long internalLocationId,
            String lotNumber,
            String serialNumber,
            Long stockLotId,
            Long stockSerialId,
            Integer quantityChange,
            Integer balanceAfter,
            OperationType operationType,
            String reason,
            LocalDateTime movementDate,
            Long referenceId,
            String referenceService,
            boolean cancelled
    ) {
    }

    public record CreateMovementRequest(
            @NotNull Long quantId,
            @NotNull Integer quantityChange,
            @NotNull OperationType operationType,
            @NotBlank String reason,
            Long referenceId,
            String referenceService
    ) {
    }

    public record NamedLotMovementRequest(
            @NotNull Long productId,
            @NotNull Long warehouseId,
            Long internalLocationId,
            @NotBlank String lotNumber,
            String serialNumber,
            @NotNull Integer quantity,
            OperationType operationType,
            @NotBlank String reason,
            Long referenceId,
            String referenceService,
            Money unitCost
    ) {
    }

    public record LotResponse(Long id, Long productId, String lotNumber, LocalDate expirationDate) {
    }

    public record SerialResponse(Long id, Long productId, Long lotId, String serialNumber) {
    }

    public record MoneyAmount(BigDecimal amount, String currency) {
        public Money toMoney() {
            return Money.fromJson(amount, currency);
        }
    }
}
