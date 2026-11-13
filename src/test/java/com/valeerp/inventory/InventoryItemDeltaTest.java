package com.valeerp.inventory;

import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.Warehouse;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryItemDeltaTest {

    @Test
    void quantityStartsAtZeroAndOnlyChangesViaDelta() {
        InventoryItem quant = open("LOTE-A", null);
        assertEquals(0, quant.getQuantity());
        quant.applyDelta(5);
        assertEquals(5, quant.getQuantity());
        quant.applyDelta(-2);
        assertEquals(3, quant.getQuantity());
    }

    @Test
    void refusesNegativeBalance() {
        InventoryItem quant = open("LOTE-B", null);
        quant.applyDelta(1);
        assertThrows(DomainException.class, () -> quant.applyDelta(-2));
    }

    @Test
    void serialQuantCannotExceedOne() {
        InventoryItem quant = open("LOTE-C", "SN-001");
        quant.applyDelta(1);
        assertThrows(DomainException.class, () -> quant.applyDelta(1));
    }

    @Test
    void refusesBlankLotName() {
        assertThrows(DomainException.class, () -> InventoryItem.normalizeLot("  "));
    }

    private static InventoryItem open(String lot, String serial) {
        Product product = Product.register(
                "SKU-T", "Producto test", 1L, 1L, 1L, null,
                Money.pen(10), 0, CostingMethod.STANDARD, Money.pen(5));
        Warehouse warehouse = Warehouse.open(1L, null, "WH1", "Almacén 1", "Av. Demo 1", true);
        return InventoryItem.open(product, warehouse, 10L, lot, serial, Money.pen(5));
    }
}
