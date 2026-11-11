package com.valeerp.mrp.stub;

import com.valeerp.mrp.port.ComponentConsumed;
import com.valeerp.mrp.port.FinishedGoodProduced;
import com.valeerp.mrp.port.InventoryMovementPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub local de movimientos MRP. No llama a {@code StockService} ni a Feign:
 * así no se rompen pickings/quants existentes hasta cablear el adaptador real.
 */
@Component
public class LoggingInventoryMovementPort implements InventoryMovementPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingInventoryMovementPort.class);

    @Override
    public void publishConsume(ComponentConsumed event) {
        log.info("MRP ComponentConsumed stub: mo={} warehouse={} product={} qty={}",
                event.manufacturingOrderId(), event.warehouseId(), event.productId(), event.quantity());
    }

    @Override
    public void publishProduce(FinishedGoodProduced event) {
        log.info("MRP FinishedGoodProduced stub: mo={} warehouse={} product={} qty={} cost={}",
                event.manufacturingOrderId(), event.warehouseId(), event.productId(),
                event.quantity(), event.unitCost());
    }
}
