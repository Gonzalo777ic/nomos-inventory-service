package com.valeerp.procurement;

import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.model.MovementType;
import com.nomos.inventory.service.model.OrderStatus;
import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.valeerp.inventory.StockService;
import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.VendorReturnDtos.CreateRequest;
import com.valeerp.procurement.VendorReturnDtos.LineRequest;
import com.valeerp.procurement.VendorReturnDtos.LineResponse;
import com.valeerp.procurement.VendorReturnDtos.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VendorReturnService {

    public static final String REFERENCE_SERVICE = "PROCUREMENT_VENDOR_RETURN";

    private final VendorReturnRepository vendorReturnRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockService stockService;
    private final GoodsReturnedPort goodsReturnedPort;
    private final Clock clock;

    public VendorReturnService(VendorReturnRepository vendorReturnRepository,
                               PurchaseOrderRepository purchaseOrderRepository,
                               InventoryItemRepository inventoryItemRepository,
                               StockService stockService,
                               GoodsReturnedPort goodsReturnedPort,
                               Clock clock) {
        this.vendorReturnRepository = vendorReturnRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockService = stockService;
        this.goodsReturnedPort = goodsReturnedPort;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Response> listByPurchaseOrder(Long purchaseOrderId) {
        return vendorReturnRepository.findByPurchaseOrderIdOrderByReturnDateDesc(purchaseOrderId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return toResponse(requireWithLines(id));
    }

    @Transactional
    public Response createDraft(CreateRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(request.purchaseOrderId())
                .orElseThrow(() -> new DomainException("Orden de compra no encontrada"));
        if (order.getStatus() != OrderStatus.COMPLETO
                && order.getStatus() != OrderStatus.CONFIRMADO
                && order.getStatus() != OrderStatus.PARCIAL) {
            throw new DomainException("Solo se devolución sobre una OC confirmada, parcial o completa");
        }
        LocalDate returnDate = request.returnDate() == null ? LocalDate.now(clock) : request.returnDate();
        VendorReturn document = VendorReturn.draft(
                order.getId(),
                order.getSupplier().getId(),
                returnDate,
                request.reason()
        );
        for (LineRequest line : request.lines()) {
            InventoryItem lot = requireLot(line.lotId(), line.productId());
            if (lot.getQuantity() < line.quantity()) {
                throw new DomainException("Stock insuficiente en el lote " + lot.getLotNumber());
            }
            document.addLine(line.productId(), line.lotId(), line.quantity(), line.unitCost());
        }
        return toResponse(vendorReturnRepository.save(document));
    }

    @Transactional
    public Response confirm(Long id) {
        VendorReturn document = requireWithLines(id);
        document.confirm();
        Instant occurredAt = Instant.now(clock);
        Long warehouseId = null;
        List<GoodsReturned.Line> eventLines = new ArrayList<>();
        for (VendorReturnLine line : document.getLines()) {
            InventoryItem lot = requireLot(line.getLotId(), line.getProductId());
            warehouseId = lot.getWarehouse().getId();
            stockService.adjustLot(
                    lot.getId(),
                    -line.getQuantity(),
                    MovementType.DEVOLUCION_PROVEEDOR,
                    "Devolución a proveedor " + document.getId(),
                    document.getId(),
                    REFERENCE_SERVICE
            );
            eventLines.add(new GoodsReturned.Line(
                    line.getProductId(),
                    line.getLotId(),
                    line.getQuantity(),
                    line.getUnitCost()));
        }
        goodsReturnedPort.publish(new GoodsReturned(
                document.getId(),
                document.getPurchaseOrderId(),
                warehouseId,
                occurredAt,
                eventLines));
        return toResponse(vendorReturnRepository.save(document));
    }

    @Transactional
    public Response cancel(Long id) {
        VendorReturn document = requireWithLines(id);
        boolean wasConfirmed = document.isConfirmed();
        document.cancel();
        if (wasConfirmed) {
            for (VendorReturnLine line : document.getLines()) {
                stockService.adjustLot(
                        line.getLotId(),
                        line.getQuantity(),
                        MovementType.DEVOLUCION_PROVEEDOR,
                        "Reverso devolución proveedor " + document.getId(),
                        document.getId(),
                        REFERENCE_SERVICE
                );
            }
        }
        return toResponse(vendorReturnRepository.save(document));
    }

    private VendorReturn requireWithLines(Long id) {
        return vendorReturnRepository.findByIdWithLines(id)
                .orElseThrow(() -> new DomainException("Devolución a proveedor no encontrada"));
    }

    private InventoryItem requireLot(Long lotId, Long productId) {
        InventoryItem lot = inventoryItemRepository.findById(lotId)
                .orElseThrow(() -> new DomainException("El lote no existe"));
        if (!lot.getProduct().getId().equals(productId)) {
            throw new DomainException("El lote no pertenece al producto indicado");
        }
        return lot;
    }

    private Response toResponse(VendorReturn document) {
        List<LineResponse> lines = document.getLines().stream()
                .map(line -> new LineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getLotId(),
                        line.getQuantity(),
                        line.getUnitCost()
                ))
                .toList();
        return new Response(
                document.getId(),
                document.getPurchaseOrderId(),
                document.getSupplierId(),
                document.getReturnDate(),
                document.getStatus(),
                document.getTotalAmount(),
                document.getReason(),
                lines
        );
    }
}
