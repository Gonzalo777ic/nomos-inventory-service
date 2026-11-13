# MRP — puertos de integración con inventario

Fecha: 10 de noviembre de 2026. Alcance: inventario de ValeERP (App MRP).

## Objetivo

La orden de fabricación (`ManufacturingOrder`) registra consumo y producción como
**hechos de dominio**. El libro de stock (`InventoryMovement` / quants / pickings)
no se muta desde el agregado MRP en este stub.

## Puerto actual

`com.valeerp.mrp.port.InventoryMovementPort`

| Método | Hecho | Efecto stub |
|---|---|---|
| `publishConsume(ComponentConsumed)` | Salida teórica de componente | Log local (`LoggingInventoryMovementPort`) |
| `publishProduce(FinishedGoodProduced)` | Entrada de producto terminado | Log local |

Sin Feign. Sin HTTP a otro servicio. Sin `StockService` en el camino crítico del stub,
para no alterar quants ni pickings existentes.

## Adaptador futuro (mismo servicio)

Cuando se cablee el libro real, un adaptador (p. ej. `StockInventoryMovementAdapter`)
implementará el puerto así:

1. **Consume** → `StockService.issueNamedLot(...)` (o equivalente) por cada
   `ComponentConsumed`, nombrando lote/serie; prohibido `findFirst()` entre lotes
   y prohibido escribir `quantity` a mano.
2. **Produce** → `StockService.receive(...)` con `FinishedGoodProduced.unitCost`
   (`Money`) y lote de fabricación.
3. Referencia del movimiento: `referenceId = manufacturingOrderId`,
   `referenceService = "MRP_MANUFACTURING_ORDER"`.
4. Anulación / scrap: movimientos compensatorios del libro, no soft-delete de OF.

El caso de uso (`ManufacturingOrderService.consume` / `produce`) no cambia de firma:
solo se sustituye el bean del puerto.

## Multi-nivel BOM

La explosión de hojas vive en `BomService.explode` / `theoreticalLeaves`.
La OF congela ese snapshot al `confirm`; el puerto solo ve productos hoja y cantidades.
