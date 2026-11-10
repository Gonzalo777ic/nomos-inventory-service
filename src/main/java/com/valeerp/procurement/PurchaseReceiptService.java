package com.valeerp.procurement;

import com.nomos.inventory.service.model.OrderStatus;
import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.PurchaseOrderDetail;
import com.nomos.inventory.service.model.Warehouse;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.StockService;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PurchaseReceiptService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;

    public PurchaseReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                                  WarehouseRepository warehouseRepository,
                                  StockService stockService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockService = stockService;
    }

    @Transactional
    public PurchaseOrder receive(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(orderId)
                .orElseThrow(() -> new DomainException("Orden de compra no encontrada"));
        if (order.getStatus() != OrderStatus.CONFIRMADO) {
            throw new DomainException("Solo se recepciona una orden confirmada por el proveedor");
        }
        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new DomainException("La orden no tiene líneas para recepcionar");
        }
        Warehouse warehouse = warehouseRepository.findByIsMainTrue()
                .orElseThrow(() -> new DomainException("No hay un almacén principal para recibir la mercadería"));
        for (PurchaseOrderDetail detail : order.getDetails()) {
            String lotNumber = "OC-" + order.getId() + "-" + detail.getProduct().getId();
            stockService.receive(
                    detail.getProduct().getId(),
                    warehouse.getId(),
                    lotNumber,
                    detail.getQuantity(),
                    BigDecimal.valueOf(detail.getUnitCost()),
                    order.getId(),
                    "PROCUREMENT",
                    "Recepción de orden " + order.getId()
            );
        }
        order.markReceived();
        return purchaseOrderRepository.save(order);
    }
}
