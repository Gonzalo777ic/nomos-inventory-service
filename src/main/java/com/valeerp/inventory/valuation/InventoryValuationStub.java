package com.valeerp.inventory.valuation;

import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * STUB LOCAL de valoración de inventario (periódica / perpetua).
 * No crea asientos ni integra Contabilidad. Ver {@code docs/inventory-valuation.md}.
 */
@Service
public class InventoryValuationStub {

    private final InventoryItemRepository itemRepository;

    public InventoryValuationStub(InventoryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public ValuationSnapshot snapshot(InventoryValuationMode mode, Long warehouseId) {
        InventoryValuationMode resolved = mode == null ? InventoryValuationMode.PERIODIC : mode;
        List<InventoryItem> quants = itemRepository.findAll().stream()
                .filter(item -> warehouseId == null || warehouseId.equals(item.getWarehouse().getId()))
                .toList();
        return build(resolved, warehouseId, quants);
    }

    private ValuationSnapshot build(InventoryValuationMode mode, Long warehouseId, List<InventoryItem> quants) {
        List<ValuationLine> lines = new ArrayList<>();
        Money total = Money.zero(Currency.PEN);
        for (InventoryItem quant : quants) {
            int qty = quant.getQuantity() == null ? 0 : quant.getQuantity();
            if (qty == 0) {
                continue;
            }
            Money unitCost = quant.getUnitCost() == null ? Money.zero(Currency.PEN) : quant.getUnitCost();
            Money lineValue = unitCost.times(qty);
            lines.add(new ValuationLine(
                    quant.getId(),
                    quant.getProduct().getId(),
                    quant.getWarehouse().getId(),
                    quant.getLotNumber(),
                    qty,
                    unitCost,
                    lineValue
            ));
            if (total.currencyCode().equals(lineValue.currencyCode())) {
                total = total.plus(lineValue);
            }
        }
        String note = mode == InventoryValuationMode.PERPETUAL
                ? "STUB PERPETUAL: mismo snapshot local; sin eventos a Contabilidad"
                : "STUB PERIODIC: valoración puntual on-hand × unitCost; sin asiento";
        return new ValuationSnapshot(mode, warehouseId, lines, total, note);
    }

    public record ValuationLine(
            Long quantId,
            Long productId,
            Long warehouseId,
            String lotNumber,
            int quantity,
            Money unitCost,
            Money lineValue
    ) {
    }

    public record ValuationSnapshot(
            InventoryValuationMode mode,
            Long warehouseId,
            List<ValuationLine> lines,
            Money totalValue,
            String stubNote
    ) {
    }
}
