package com.valeerp.inventory;

import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.service.repository.InventoryMovementRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.location.InternalLocationRepository;
import com.valeerp.inventory.stock.StockLotRepository;
import com.valeerp.inventory.stock.StockQuantityPolicy;
import com.valeerp.inventory.stock.StockSerialRepository;
import com.valeerp.kernel.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StockServiceNamedLotTest {

    @Mock private InventoryItemRepository itemRepository;
    @Mock private InventoryMovementRepository movementRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private InternalLocationRepository locationRepository;
    @Mock private StockLotRepository lotRepository;
    @Mock private StockSerialRepository serialRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(
                itemRepository, movementRepository, productRepository, warehouseRepository,
                locationRepository, lotRepository, serialRepository,
                new StockQuantityPolicy(false, 30, 0),
                Clock.systemUTC());
    }

    @Test
    void issueByExpiryWithoutNamedLotIsRejected() {
        DomainException ex = assertThrows(DomainException.class,
                () -> stockService.issueByExpiry(1L, 5, null, "venta", 9L, "STORE"));
        assertTrue(ex.getMessage().contains("lotNumber") || ex.getMessage().toLowerCase().contains("lote"));
    }

    @Test
    void adjustLotRequiresQuantId() {
        DomainException ex = assertThrows(DomainException.class,
                () -> stockService.adjustLot(null, 1, OperationType.ADJUSTMENT, "ajuste", null, null));
        assertTrue(ex.getMessage().toLowerCase().contains("lote")
                || ex.getMessage().toLowerCase().contains("quant"));
    }
}
