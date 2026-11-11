package com.valeerp.inventory.reorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class ReorderRuleDtos {

    private ReorderRuleDtos() {
    }

    public record CreateReorderRuleRequest(
            @NotNull Long productId,
            @NotNull Long warehouseId,
            @NotNull @Min(0) Integer minQuantity,
            @NotNull @Min(0) Integer maxQuantity
    ) {
    }

    public record UpdateReorderRuleRequest(
            @NotNull @Min(0) Integer minQuantity,
            @NotNull @Min(0) Integer maxQuantity
    ) {
    }

    public record ReorderRuleResponse(
            Long id,
            Long productId,
            Long warehouseId,
            Integer minQuantity,
            Integer maxQuantity
    ) {
    }

    public record ReplenishmentSuggestionResponse(
            Long ruleId,
            Long productId,
            Long warehouseId,
            Integer onHand,
            Integer minQuantity,
            Integer maxQuantity,
            Integer suggestedQuantity
    ) {
    }
}
