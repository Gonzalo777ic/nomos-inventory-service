package com.valeerp.procurement;

/**
 * Puerto de publicación del hecho GoodsReturned. Sin implementación Feign.
 */
public interface GoodsReturnedPort {

    void publish(GoodsReturned event);
}
