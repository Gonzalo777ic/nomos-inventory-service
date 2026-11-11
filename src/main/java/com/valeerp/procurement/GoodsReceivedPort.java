package com.valeerp.procurement;

/**
 * Puerto de publicación del hecho GoodsReceived. Sin implementación Feign.
 */
public interface GoodsReceivedPort {

    void publish(GoodsReceived event);
}
