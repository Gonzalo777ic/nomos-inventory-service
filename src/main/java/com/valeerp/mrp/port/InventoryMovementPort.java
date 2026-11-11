package com.valeerp.mrp.port;

/**
 * Puerto de salida MRP → libro de inventario.
 * <p>
 * Hoy: adaptador stub (log). Mañana: adaptador que traduzca a
 * {@code StockService.issueNamedLot} / {@code receive} sin Feign cruzado
 * (mismo servicio) o a un bus de hechos. Ver {@code docs/MRP-PORTS.md}.
 * <p>
 * No mutar quants desde el agregado de OF; solo publicar hechos.
 */
public interface InventoryMovementPort {

    void publishConsume(ComponentConsumed event);

    void publishProduce(FinishedGoodProduced event);
}
