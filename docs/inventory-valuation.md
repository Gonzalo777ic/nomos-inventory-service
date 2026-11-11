# Inventory valuation stub (local)

**Status:** documented stub inside `nomos-inventory-service`.  
**No Feign / no cross-accounting integration.** Contabilidad remains owner of journal entries.

## Modes

| Mode | Meaning in this stub |
|---|---|
| `PERIODIC` | Snapshot: sum of `onHand × unitCost` (Money) over quants (`InventoryItem`). |
| `PERPETUAL` | Same snapshot today; intended future hook when each `InventoryMovement` would propose a valuation event to Accounting. |

## API

`GET /api/inventory/valuation?mode=PERIODIC|PERPETUAL&warehouseId=` (optional warehouse filter)

Returns a read-only `ValuationSnapshot` with lines per quant and a Money total. Currency follows each quant’s `unitCost` (PEN first).

## Explicit non-goals

- No `JournalEntry` creation.
- No Feign to `nomos-store-service` Contabilidad.
- No posting of COGS / stock valuation accounts.

When Contabilidad is ready, replace this stub with a published port (e.g. `InventoryValued` event) owned by inventory and consumed by accounting — still without Feign from UI.
