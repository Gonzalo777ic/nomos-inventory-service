package com.valeerp.mrp;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.mrp.BillOfMaterials.TheoreticalNeed;
import com.valeerp.mrp.ManufacturingOrderDtos.ComponentLineResponse;
import com.valeerp.mrp.ManufacturingOrderDtos.CreateRequest;
import com.valeerp.mrp.ManufacturingOrderDtos.Response;
import com.valeerp.mrp.port.ComponentConsumed;
import com.valeerp.mrp.port.FinishedGoodProduced;
import com.valeerp.mrp.port.InventoryMovementPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Casos de uso de OF. Consume/produce publican stubs vía {@link InventoryMovementPort};
 * no mutan quants ni pickings existentes.
 */
@Service
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository orderRepository;
    private final BomService bomService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementPort inventoryMovementPort;
    private final Clock clock;

    public ManufacturingOrderService(ManufacturingOrderRepository orderRepository,
                                     BomService bomService,
                                     ProductRepository productRepository,
                                     WarehouseRepository warehouseRepository,
                                     InventoryMovementPort inventoryMovementPort,
                                     Clock clock) {
        this.orderRepository = orderRepository;
        this.bomService = bomService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryMovementPort = inventoryMovementPort;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Response> listByWarehouse(Long warehouseId) {
        return orderRepository.findByWarehouseIdOrderByIdDesc(warehouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return toResponse(requireWithLines(id));
    }

    @Transactional
    public Response create(CreateRequest request) {
        if (!warehouseRepository.existsById(request.warehouseId())) {
            throw new DomainException("Almacén no encontrado");
        }
        BillOfMaterials bom = bomService.requireWithLines(request.bomId());
        if (bom.getStatus() != BomStatus.ACTIVE) {
            throw new DomainException("La OF exige una BOM activa");
        }
        String code = request.code().trim().toUpperCase();
        if (orderRepository.existsByCode(code)) {
            throw new DomainException("Ya existe una OF con código " + code);
        }
        Product product = productRepository.findById(bom.getProductId())
                .orElseThrow(() -> new DomainException("Producto terminado no encontrado"));
        Money unitCost = request.unitCost() == null ? product.valuationCost() : request.unitCost();
        ManufacturingOrder order = ManufacturingOrder.draft(
                code,
                bom.getId(),
                bom.getProductId(),
                request.warehouseId(),
                request.quantityToProduce(),
                unitCost,
                request.notes()
        );
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public Response confirm(Long id) {
        ManufacturingOrder order = requireWithLines(id);
        List<TheoreticalNeed> theoretical = bomService.theoreticalLeaves(
                order.getBomId(), order.getQuantityToProduce());
        order.confirm(theoretical, Instant.now(clock));
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public Response start(Long id) {
        ManufacturingOrder order = requireWithLines(id);
        order.start(Instant.now(clock));
        return toResponse(orderRepository.save(order));
    }

    /**
     * Consumo teórico → hechos stub {@link ComponentConsumed}. Sin StockService.
     */
    @Transactional
    public Response consume(Long id) {
        ManufacturingOrder order = requireWithLines(id);
        Instant occurredAt = Instant.now(clock);
        List<MoComponentLine> consumed = order.consumeComponents();
        for (MoComponentLine line : consumed) {
            inventoryMovementPort.publishConsume(new ComponentConsumed(
                    order.getId(),
                    order.getWarehouseId(),
                    line.getProductId(),
                    line.getConsumedQuantity(),
                    occurredAt
            ));
        }
        return toResponse(orderRepository.save(order));
    }

    /**
     * Producción del terminado → hecho stub {@link FinishedGoodProduced}. Sin StockService.
     */
    @Transactional
    public Response produce(Long id) {
        ManufacturingOrder order = requireWithLines(id);
        Instant occurredAt = Instant.now(clock);
        order.produceFinished(occurredAt);
        inventoryMovementPort.publishProduce(new FinishedGoodProduced(
                order.getId(),
                order.getWarehouseId(),
                order.getProductId(),
                order.getQuantityProduced(),
                order.getUnitCost(),
                occurredAt
        ));
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public Response cancel(Long id) {
        ManufacturingOrder order = requireWithLines(id);
        order.cancel();
        return toResponse(orderRepository.save(order));
    }

    private ManufacturingOrder requireWithLines(Long id) {
        return orderRepository.findByIdWithLines(id)
                .orElseThrow(() -> new DomainException("Orden de fabricación no encontrada"));
    }

    private Response toResponse(ManufacturingOrder order) {
        List<ComponentLineResponse> components = order.getComponentLines().stream()
                .map(line -> new ComponentLineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getPlannedQuantity(),
                        line.getConsumedQuantity()
                ))
                .toList();
        return new Response(
                order.getId(),
                order.getCode(),
                order.getBomId(),
                order.getProductId(),
                order.getWarehouseId(),
                order.getQuantityToProduce(),
                order.getQuantityProduced(),
                order.getStatus(),
                order.getUnitCost(),
                order.getNotes(),
                order.getConfirmedAt(),
                order.getStartedAt(),
                order.getCompletedAt(),
                components
        );
    }
}
