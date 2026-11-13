package com.valeerp.inventory;

import com.valeerp.inventory.service.model.InventoryMovement;
import com.valeerp.inventory.service.repository.InventoryMovementRepository;
import com.valeerp.inventory.StockDtos.CreateMovementRequest;
import com.valeerp.inventory.StockDtos.MovementResponse;
import com.valeerp.inventory.StockDtos.NamedLotMovementRequest;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovementQueryService {

    private final InventoryMovementRepository movementRepository;
    private final StockService stockService;

    public MovementQueryService(InventoryMovementRepository movementRepository, StockService stockService) {
        this.movementRepository = movementRepository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> listAll() {
        return movementRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MovementResponse get(Long id) {
        return toResponse(movementRepository.findById(id)
                .orElseThrow(() -> new DomainException("Movimiento de inventario no encontrado")));
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> byProduct(Long productId) {
        return movementRepository.findByProductIdOrderByMovementDateDesc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MovementResponse create(CreateMovementRequest request) {
        InventoryMovement created = stockService.adjustLot(
                request.quantId(),
                request.quantityChange(),
                request.operationType(),
                request.reason(),
                request.referenceId(),
                request.referenceService()
        );
        return toResponse(created);
    }

    @Transactional
    public MovementResponse receiveNamed(NamedLotMovementRequest request) {
        InventoryMovement created = stockService.receive(
                request.productId(),
                request.warehouseId(),
                request.internalLocationId(),
                request.lotNumber(),
                request.serialNumber(),
                request.quantity(),
                request.unitCost(),
                request.operationType() == null ? OperationType.RECEIPT : request.operationType(),
                request.referenceId(),
                request.referenceService(),
                request.reason()
        );
        return toResponse(created);
    }

    @Transactional
    public MovementResponse issueNamed(NamedLotMovementRequest request) {
        InventoryMovement created = stockService.issueNamedLot(
                request.productId(),
                request.warehouseId(),
                request.internalLocationId(),
                request.lotNumber(),
                request.serialNumber(),
                request.quantity(),
                request.operationType() == null ? OperationType.DELIVERY : request.operationType(),
                request.reason(),
                request.referenceId(),
                request.referenceService()
        );
        return toResponse(created);
    }

    private MovementResponse toResponse(InventoryMovement movement) {
        Long quantId = movement.getInventoryItem() == null ? null : movement.getInventoryItem().getId();
        Long productId = movement.getProduct() == null ? null : movement.getProduct().getId();
        return new MovementResponse(
                movement.getId(),
                quantId,
                productId,
                movement.getWarehouseId(),
                movement.getInternalLocationId(),
                movement.getLotNumber(),
                movement.getSerialNumber(),
                movement.getStockLotId(),
                movement.getStockSerialId(),
                movement.getQuantityChange(),
                movement.getBalanceAfter(),
                movement.getOperationType(),
                movement.getReason(),
                movement.getMovementDate(),
                movement.getReferenceId(),
                movement.getReferenceService(),
                movement.isCancelled()
        );
    }
}
