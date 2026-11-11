package com.valeerp.procurement;

import com.valeerp.kernel.Money;

import java.time.Instant;
import java.util.List;

/**
 * Hecho de devolución a proveedor. Inventario aplicará el egreso vía adaptador futuro;
 * el puerto stub no llama a otros servicios.
 */
public record GoodsReturned(
        Long vendorReturnId,
        Long purchaseOrderId,
        Long warehouseId,
        Instant occurredAt,
        List<Line> lines
) {
    public record Line(
            Long productId,
            Long lotId,
            int quantity,
            Money unitCost
    ) {
    }
}
