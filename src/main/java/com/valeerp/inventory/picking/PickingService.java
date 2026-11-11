package com.valeerp.inventory.picking;

import com.nomos.inventory.service.model.InventoryBookRefs;
import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.model.PickingStatus;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.OperationType;
import com.valeerp.inventory.OperationTypePolicy;
import com.valeerp.inventory.StockReservationService;
import com.valeerp.inventory.StockService;
import com.valeerp.inventory.location.InternalLocation;
import com.valeerp.inventory.location.InternalLocationRepository;
import com.valeerp.inventory.location.InternalLocationType;
import com.valeerp.inventory.picking.PickingDtos.AddPickingLineRequest;
import com.valeerp.inventory.picking.PickingDtos.CreatePickingRequest;
import com.valeerp.inventory.picking.PickingDtos.PickingLineResponse;
import com.valeerp.inventory.picking.PickingDtos.PickingResponse;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class PickingService {

    private final PickingRepository pickingRepository;
    private final StockService stockService;
    private final StockReservationService reservationService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InternalLocationRepository locationRepository;
    private final Clock clock;

    public PickingService(PickingRepository pickingRepository,
                          StockService stockService,
                          StockReservationService reservationService,
                          ProductRepository productRepository,
                          WarehouseRepository warehouseRepository,
                          InternalLocationRepository locationRepository,
                          Clock clock) {
        this.pickingRepository = pickingRepository;
        this.stockService = stockService;
        this.reservationService = reservationService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PickingResponse> listByCompany(Long companyId) {
        requireCompany(companyId);
        return pickingRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PickingResponse get(Long id) {
        return toResponse(requirePicking(id));
    }

    @Transactional
    public PickingResponse create(CreatePickingRequest request) {
        requireCompany(request.companyId());
        requireWarehouse(request.sourceWarehouseId());
        if (request.destinationWarehouseId() != null) {
            requireWarehouse(request.destinationWarehouseId());
        }
        String code = request.code().trim().toUpperCase();
        if (pickingRepository.existsByCompanyIdAndCode(request.companyId(), code)) {
            throw new DomainException("Ya existe un picking con código " + code);
        }
        Picking picking = Picking.draft(
                request.companyId(),
                code,
                request.operationType(),
                request.sourceWarehouseId(),
                request.destinationWarehouseId(),
                request.notes(),
                Actors.current(),
                Instant.now(clock)
        );
        for (var line : request.lines()) {
            requireProduct(line.productId());
            picking.addLine(
                    line.productId(),
                    line.quantity(),
                    line.inventoryItemId(),
                    line.lotNumber(),
                    line.serialNumber(),
                    line.sourceLocationId(),
                    line.destinationLocationId()
            );
        }
        return toResponse(pickingRepository.save(picking));
    }

    @Transactional
    public PickingResponse addLine(Long pickingId, AddPickingLineRequest request) {
        Picking picking = requirePicking(pickingId);
        requireProduct(request.productId());
        picking.addLine(
                request.productId(),
                request.quantity(),
                request.inventoryItemId(),
                request.lotNumber(),
                request.serialNumber(),
                request.sourceLocationId(),
                request.destinationLocationId()
        );
        return toResponse(pickingRepository.save(picking));
    }

    /** Confirma y reserva stock en quants nombrados (sin escribir quantity a mano). */
    @Transactional
    public PickingResponse confirm(Long id) {
        Picking picking = requirePicking(id);
        picking.confirm(Instant.now(clock));
        if (picking.reservesStock()) {
            for (PickingLine line : picking.mutableLines()) {
                InventoryItem quant = reservationService.resolveNamedQuant(
                        line.getProductId(),
                        picking.getSourceWarehouseId(),
                        line.getSourceLocationId(),
                        line.getInventoryItemId(),
                        line.getLotNumber(),
                        line.getSerialNumber()
                );
                line.bindQuant(quant.getId(), quant.getLotNumber());
                reservationService.reserveQuant(quant.getId(), line.getQuantity());
                line.markReserved();
            }
        }
        return toResponse(pickingRepository.save(picking));
    }

    @Transactional
    public PickingResponse complete(Long id) {
        Picking picking = requirePicking(id);
        String reason = "Picking " + picking.getCode();
        for (PickingLine line : picking.mutableLines()) {
            completeLine(picking, line, reason);
        }
        picking.complete(Instant.now(clock));
        return toResponse(pickingRepository.save(picking));
    }

    @Transactional
    public PickingResponse cancel(Long id) {
        Picking picking = requirePicking(id);
        if (picking.getStatus() == PickingStatus.CONFIRMED && picking.reservesStock()) {
            for (PickingLine line : picking.mutableLines()) {
                if (line.isReserved() && line.getInventoryItemId() != null) {
                    reservationService.releaseQuantReservation(line.getInventoryItemId(), line.getQuantity());
                    line.clearReserved();
                }
            }
        }
        picking.cancel(Instant.now(clock));
        return toResponse(pickingRepository.save(picking));
    }

    private void completeLine(Picking picking, PickingLine line, String reason) {
        OperationType type = picking.getOperationType();
        Long refId = picking.getId();
        switch (type) {
            case RECEIPT -> stockService.receive(
                    line.getProductId(),
                    picking.getSourceWarehouseId(),
                    line.getDestinationLocationId(),
                    line.getLotNumber(),
                    line.getSerialNumber(),
                    line.getQuantity(),
                    Money.zero(Currency.PEN),
                    OperationType.RECEIPT,
                    refId,
                    InventoryBookRefs.PICKING,
                    reason
            );
            case DELIVERY, SCRAP, ADJUSTMENT -> {
                requireReservedLine(line);
                reservationService.completeReservedIssue(
                        line.getInventoryItemId(),
                        line.getQuantity(),
                        type,
                        reason,
                        refId,
                        InventoryBookRefs.PICKING
                );
                line.clearReserved();
            }
            case INTERNAL_TRANSFER -> {
                requireReservedLine(line);
                reservationService.completeReservedIssue(
                        line.getInventoryItemId(),
                        line.getQuantity(),
                        OperationType.INTERNAL_TRANSFER,
                        reason + " (salida)",
                        refId,
                        InventoryBookRefs.PICKING
                );
                line.clearReserved();
                Long destWarehouse = picking.getDestinationWarehouseId() == null
                        ? picking.getSourceWarehouseId()
                        : picking.getDestinationWarehouseId();
                Long destLocation = line.getDestinationLocationId() != null
                        ? line.getDestinationLocationId()
                        : defaultLocation(destWarehouse, OperationTypePolicy.defaultDestination(type));
                stockService.receive(
                        line.getProductId(),
                        destWarehouse,
                        destLocation,
                        line.getLotNumber(),
                        line.getSerialNumber(),
                        line.getQuantity(),
                        Money.zero(Currency.PEN),
                        OperationType.INTERNAL_TRANSFER,
                        refId,
                        InventoryBookRefs.PICKING,
                        reason + " (entrada)"
                );
            }
            default -> throw new DomainException("Tipo de operación no soportado: " + type);
        }
    }

    private void requireReservedLine(PickingLine line) {
        if (!line.isReserved() || line.getInventoryItemId() == null) {
            throw new DomainException("La línea no tiene reserva/quant para completar");
        }
    }

    private Long defaultLocation(Long warehouseId, InternalLocationType type) {
        return locationRepository.findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(warehouseId, type)
                .map(InternalLocation::getId)
                .orElseGet(() -> locationRepository.save(InternalLocation.defaultOf(warehouseId, type)).getId());
    }

    private Picking requirePicking(Long id) {
        return pickingRepository.findById(id)
                .orElseThrow(() -> new DomainException("El picking no existe"));
    }

    private void requireCompany(Long companyId) {
        if (companyId == null) {
            throw new DomainException("companyId es obligatorio");
        }
    }

    private void requireWarehouse(Long warehouseId) {
        if (warehouseId == null || !warehouseRepository.existsById(warehouseId)) {
            throw new DomainException("El almacén no existe");
        }
    }

    private void requireProduct(Long productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new DomainException("El producto no existe");
        }
    }

    private PickingResponse toResponse(Picking picking) {
        List<PickingLineResponse> lines = picking.getLines().stream()
                .map(line -> new PickingLineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getQuantity(),
                        line.getInventoryItemId(),
                        line.getLotNumber(),
                        line.getSerialNumber(),
                        line.getSourceLocationId(),
                        line.getDestinationLocationId(),
                        line.isReserved()
                ))
                .toList();
        return new PickingResponse(
                picking.getId(),
                picking.getCompanyId(),
                picking.getCode(),
                picking.getOperationType(),
                picking.getStatus(),
                picking.getSourceWarehouseId(),
                picking.getDestinationWarehouseId(),
                picking.getNotes(),
                picking.getCreatedAt(),
                picking.getConfirmedAt(),
                picking.getCompletedAt(),
                picking.getCancelledAt(),
                picking.getCreatedBy(),
                lines
        );
    }
}
