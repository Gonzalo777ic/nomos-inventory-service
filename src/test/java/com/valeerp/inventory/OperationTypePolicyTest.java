package com.valeerp.inventory;

import com.valeerp.inventory.location.InternalLocationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationTypePolicyTest {

    @Test
    void receiptGoesFromInputToStock() {
        assertEquals(InternalLocationType.INPUT, OperationTypePolicy.defaultSource(OperationType.RECEIPT));
        assertEquals(InternalLocationType.STOCK, OperationTypePolicy.defaultDestination(OperationType.RECEIPT));
        assertTrue(OperationTypePolicy.increasesOnHand(OperationType.RECEIPT));
    }

    @Test
    void deliveryGoesFromStockToOutput() {
        assertEquals(InternalLocationType.STOCK, OperationTypePolicy.defaultSource(OperationType.DELIVERY));
        assertEquals(InternalLocationType.OUTPUT, OperationTypePolicy.defaultDestination(OperationType.DELIVERY));
        assertFalse(OperationTypePolicy.increasesOnHand(OperationType.DELIVERY));
    }

    @Test
    void scrapGoesFromStockToScrap() {
        assertEquals(InternalLocationType.STOCK, OperationTypePolicy.defaultSource(OperationType.SCRAP));
        assertEquals(InternalLocationType.SCRAP, OperationTypePolicy.defaultDestination(OperationType.SCRAP));
    }

    @Test
    void internalTransferStaysInStockFamily() {
        assertEquals(InternalLocationType.STOCK, OperationTypePolicy.defaultSource(OperationType.INTERNAL_TRANSFER));
        assertEquals(InternalLocationType.STOCK, OperationTypePolicy.defaultDestination(OperationType.INTERNAL_TRANSFER));
    }
}
