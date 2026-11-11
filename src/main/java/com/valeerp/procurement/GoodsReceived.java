package com.valeerp.procurement;

import com.valeerp.kernel.Money;

import java.time.Instant;
import java.util.List;

/**
 * Hecho de recepción de mercadería. Inventario lo aplicará vía adaptador futuro;
 * aquí solo se publica por el puerto stub.
 */
public record GoodsReceived(
        Long purchaseOrderId,
        Long warehouseId,
        Instant occurredAt,
        List<Line> lines
) {
    public record Line(
            Long productId,
            String lotNumber,
            int quantity,
            Money unitCost
    ) {
    }
}
