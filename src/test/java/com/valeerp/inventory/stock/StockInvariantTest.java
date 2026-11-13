package com.valeerp.inventory.stock;

import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.InventoryMovement;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.Warehouse;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.inventory.OperationType;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Invariantes de stock (#44) solo dentro del servicio: sin Feign/Auth0.
 */
class StockInvariantTest {

    @Test
    void applyDeltaRejectsNegativeWhenPolicyForbids() {
        InventoryItem quant = openQuant("LOT-A", null);
        quant.applyDelta(5, false);

        DomainException ex = assertThrows(DomainException.class, () -> quant.applyDelta(-6, false));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        assertEquals(5, quant.getQuantity());
    }

    @Test
    void applyDeltaAllowsNegativeWhenPolicyAllows() {
        InventoryItem quant = openQuant("LOT-B", null);
        quant.applyDelta(2, true);
        quant.applyDelta(-5, true);
        assertEquals(-3, quant.getQuantity());
    }

    @Test
    void stockQuantityPolicyEncodesThresholdsFromSettingsNotServiceLiterals() {
        StockQuantitySettings settings = new StockQuantitySettings();
        settings.setAllowNegative(false);
        settings.setNearExpiryDays(10);
        settings.setCriticalAbsoluteQty(0);
        StockQuantityPolicy policy = StockQuantityPolicy.from(settings);

        assertTrue(policy.isBelowProductThreshold(3, 5));
        assertTrue(policy.isCritical(0));
        assertTrue(policy.isNearExpiry(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 10)));
        assertTrue(policy.isExpired(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 10)));
    }

    @Test
    void movementRequiresNamedLotAndStockLotMaster() {
        InventoryItem quant = openQuant("LOT-TRACE", null);
        quant.applyDelta(1, false);

        assertThrows(DomainException.class,
                () -> InventoryMovement.record(
                        quant, 1, OperationType.RECEIPT, "entrada", null, null,
                        LocalDateTime.now(), null, null));

        InventoryMovement movement = InventoryMovement.record(
                quant, 1, OperationType.RECEIPT, "entrada", 9L, "TEST",
                LocalDateTime.now(), 100L, null);
        assertEquals("LOT-TRACE", movement.getLotNumber());
        assertEquals(100L, movement.getStockLotId());
    }

    @Test
    void serialTrackedMovementRequiresSerialMaster() {
        InventoryItem quant = openQuant("LOT-S", "SN-1");
        quant.applyDelta(1, false);

        assertThrows(DomainException.class,
                () -> InventoryMovement.record(
                        quant, 1, OperationType.RECEIPT, "serie", null, null,
                        LocalDateTime.now(), 10L, null));

        InventoryMovement movement = InventoryMovement.record(
                quant, 1, OperationType.RECEIPT, "serie", null, null,
                LocalDateTime.now(), 10L, 20L);
        assertEquals("SN-1", movement.getSerialNumber());
        assertEquals(20L, movement.getStockSerialId());
    }

    @Test
    void untrackedLotMarkerIsRejectedByTraceabilityPolicy() throws Exception {
        InventoryItem quant = openQuant("TMP", null);
        Field lot = InventoryItem.class.getDeclaredField("lotNumber");
        lot.setAccessible(true);
        lot.set(quant, "-");
        assertThrows(DomainException.class, () -> LotTraceabilityPolicy.assertQuantReady(quant));
    }

    private static InventoryItem openQuant(String lotNumber, String serial) {
        Product product = Product.register(
                "SKU-" + lotNumber,
                "Producto " + lotNumber,
                1L, 1L, 1L,
                null,
                Money.pen(new BigDecimal("10.00")),
                5,
                CostingMethod.STANDARD,
                Money.pen(new BigDecimal("4.00"))
        );
        setId(product, 1L);
        Warehouse warehouse = Warehouse.open(1L, null, "MAIN", "Principal", "Av. Demo 1", true);
        setId(warehouse, 1L);
        return InventoryItem.open(product, warehouse, 10L, lotNumber, serial, Money.pen(new BigDecimal("4.00")));
    }

    private static void setId(Object entity, Long id) {
        Class<?> type = entity.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalStateException("Sin campo id en " + entity.getClass());
    }
}
