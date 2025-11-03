package com.nomos.inventory.service.model;

// Tipos de movimientos de inventario que afectan el stock.
public enum MovementType {
    ENTRADA,        // Ingreso por nueva compra o recepción de proveedor (REQ-FUN-01)
    SALIDA_VENTA,   // Salida por venta al cliente (REQ-FUN-02)
    AJUSTE_DEVOLUCION, // Ajuste manual por devolución (aumenta stock) (REQ-FUN-11)
    AJUSTE_PERDIDA,    // Ajuste manual por merma, daño o pérdida (disminuye stock) (REQ-FUN-02, REQ-FUN-11)
    TRANSFERENCIA   // Movimiento entre ubicaciones/almacenes (si se implementa WarehouseLocation)
}
