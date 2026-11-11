package com.valeerp.procurement;

import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.PurchaseOrderDetail;
import com.nomos.inventory.service.model.Warehouse;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.StockService;
import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.PurchaseOrderDtos.ReceiptLineRequest;
import com.valeerp.procurement.PurchaseOrderDtos.ReceivePurchaseOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseReceiptService {

    private static final String REFERENCE_SERVICE = "PROCUREMENT";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final GoodsReceivedPort goodsReceivedPort;
    private final Clock clock;

    public PurchaseReceiptService(PurchaseOrderRepository purchaseOrderRepository,
                                  WarehouseRepository warehouseRepository,
                                  StockService stockService,
                                  GoodsReceivedPort goodsReceivedPort,
                                  Clock clock) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockService = stockService;
        this.goodsReceivedPort = goodsReceivedPort;
        this.clock = clock;
    }

    /**
     * Recepción parcial o total. El agregado decide el estado a partir del residual.
     */
    @Transactional
    public PurchaseOrder receive(Long orderId, ReceivePurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(orderId)
                .orElseThrow(() -> new DomainException("Orden de compra no encontrada"));
        Warehouse warehouse = requireWarehouse(request.warehouseId());

        Map<Long, Integer> before = snapshotReceived(order);
        if (request.lines() == null || request.lines().isEmpty()) {
            order.receiveAllResidual();
        } else {
            Map<Long, Integer> quantities = new HashMap<>();
            for (ReceiptLineRequest line : request.lines()) {
                quantities.merge(line.detailId(), line.quantity(), Integer::sum);
            }
            order.receive(quantities);
        }

        List<GoodsReceived.Line> eventLines = new ArrayList<>();
        for (PurchaseOrderDetail detail : order.getDetails()) {
            int previous = before.getOrDefault(detail.getId(), 0);
            int current = detail.getReceivedQuantity() == null ? 0 : detail.getReceivedQuantity();
            int delta = current - previous;
            if (delta <= 0) {
                continue;
            }
            String lotNumber = "OC-" + order.getId() + "-" + detail.getProduct().getId();
            stockService.receive(
                    detail.getProduct().getId(),
                    warehouse.getId(),
                    lotNumber,
                    delta,
                    detail.getUnitCost(),
                    order.getId(),
                    REFERENCE_SERVICE,
                    "Recepción OC " + order.getId() + " línea " + detail.getId()
            );
            eventLines.add(new GoodsReceived.Line(
                    detail.getProduct().getId(),
                    lotNumber,
                    delta,
                    detail.getUnitCost()));
        }
        if (eventLines.isEmpty()) {
            throw new DomainException("La recepción no aplicó cantidad alguna");
        }
        goodsReceivedPort.publish(new GoodsReceived(
                order.getId(),
                warehouse.getId(),
                Instant.now(clock),
                eventLines));
        return purchaseOrderRepository.save(order);
    }

    /**
     * Compatibilidad con el flujo legado: recibe todo el residual en el almacén principal.
     */
    @Transactional
    public PurchaseOrder receive(Long orderId) {
        Warehouse warehouse = warehouseRepository.findByIsMainTrue()
                .orElseThrow(() -> new DomainException("No hay un almacén principal para recibir la mercadería"));
        return receive(orderId, new ReceivePurchaseOrderRequest(warehouse.getId(), List.of()));
    }

    private Warehouse requireWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new DomainException("La recepción exige un almacén");
        }
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("El almacén no existe"));
    }

    private Map<Long, Integer> snapshotReceived(PurchaseOrder order) {
        Map<Long, Integer> snapshot = new HashMap<>();
        if (order.getDetails() == null) {
            return snapshot;
        }
        for (PurchaseOrderDetail detail : order.getDetails()) {
            snapshot.put(detail.getId(), detail.getReceivedQuantity() == null ? 0 : detail.getReceivedQuantity());
        }
        return snapshot;
    }
}
