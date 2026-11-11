package com.valeerp.procurement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub local de puertos de hechos de abastecimiento. No llama a otros servicios.
 */
@Component
public class LoggingGoodsPorts implements GoodsReceivedPort, GoodsReturnedPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingGoodsPorts.class);

    @Override
    public void publish(GoodsReceived event) {
        log.info("GoodsReceived stub: order={} warehouse={} lines={}",
                event.purchaseOrderId(), event.warehouseId(),
                event.lines() == null ? 0 : event.lines().size());
    }

    @Override
    public void publish(GoodsReturned event) {
        log.info("GoodsReturned stub: return={} order={} warehouse={} lines={}",
                event.vendorReturnId(), event.purchaseOrderId(), event.warehouseId(),
                event.lines() == null ? 0 : event.lines().size());
    }
}
