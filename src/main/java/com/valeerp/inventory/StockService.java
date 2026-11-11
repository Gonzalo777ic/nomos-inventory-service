package com.valeerp.inventory;

import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.model.InventoryMovement;
import com.nomos.inventory.service.model.MovementType;
import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.Warehouse;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.InventoryMovementRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.location.InternalLocation;
import com.valeerp.inventory.location.InternalLocationRepository;
import com.valeerp.inventory.location.InternalLocationType;
import com.valeerp.inventory.stock.LotTraceabilityPolicy;
import com.valeerp.inventory.stock.StockLot;
import com.valeerp.inventory.stock.StockLotRepository;
import com.valeerp.inventory.stock.StockQuantityPolicy;
import com.valeerp.inventory.stock.StockSerial;
import com.valeerp.inventory.stock.StockSerialRepository;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Único escritor de cantidad on-hand. Cada movimiento del libro lleva lote/serie.
 */
@Service
public class StockService {

    private final InventoryItemRepository itemRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InternalLocationRepository locationRepository;
    private final StockLotRepository stockLotRepository;
    private final StockSerialRepository stockSerialRepository;
    private final StockQuantityPolicy quantityPolicy;
    private final Clock clock;

    public StockService(InventoryItemRepository itemRepository,
                        InventoryMovementRepository movementRepository,
                        ProductRepository productRepository,
                        WarehouseRepository warehouseRepository,
                        InternalLocationRepository locationRepository,
                        StockLotRepository stockLotRepository,
                        StockSerialRepository stockSerialRepository,
                        StockQuantityPolicy quantityPolicy,
                        Clock clock) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationRepository = locationRepository;
        this.stockLotRepository = stockLotRepository;
        this.stockSerialRepository = stockSerialRepository;
        this.quantityPolicy = quantityPolicy;
        this.clock = clock;
    }

    @Transactional
    public InventoryMovement adjustLot(Long quantId, int delta, MovementType type, String reason,
                                       Long referenceId, String referenceService) {
        OperationType operation = type == null ? OperationType.ADJUSTMENT : type.toOperationType();
        return adjustLot(quantId, delta, operation, reason, referenceId, referenceService);
    }

    @Transactional
    public InventoryMovement adjustLot(Long quantId, int delta, OperationType operationType, String reason,
                                       Long referenceId, String referenceService) {
        if (delta == 0) {
            throw new DomainException("El movimiento no puede ser cero");
        }
        return apply(requireQuant(quantId), delta,
                operationType == null ? OperationType.ADJUSTMENT : operationType,
                reason, referenceId, referenceService);
    }

    @Transactional
    public InventoryMovement receive(Long productId, Long warehouseId, Long locationId,
                                     String lotNumber, String serialNumber, int quantity,
                                     Money unitCost, OperationType operationType,
                                     Long referenceId, String referenceService, String reason) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad recibida debe ser mayor que cero");
        }
        InventoryItem quant = findOrOpenQuant(productId, warehouseId, locationId, lotNumber, serialNumber, unitCost);
        return apply(quant, quantity,
                operationType == null ? OperationType.RECEIPT : operationType,
                reason, referenceId, referenceService);
    }

    /** Compatibilidad recepción OC: ubicación STOCK por defecto del almacén. */
    @Transactional
    public InventoryMovement receive(Long productId, Long warehouseId, String lotNumber, int quantity,
                                     Money unitCost, Long referenceId, String referenceService, String reason) {
        return receive(productId, warehouseId, null, lotNumber, null, quantity, unitCost,
                OperationType.RECEIPT, referenceId, referenceService, reason);
    }

    @Transactional
    public InventoryMovement issueNamedLot(Long productId, Long warehouseId, Long locationId,
                                           String lotNumber, String serialNumber, int quantity,
                                           OperationType operationType, String reason,
                                           Long referenceId, String referenceService) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad a descontar debe ser mayor que cero");
        }
        String normalizedLot = InventoryItem.normalizeLot(lotNumber);
        String normalizedSerial = InventoryItem.normalizeSerial(serialNumber);
        Long resolvedLocation = resolveLocationId(warehouseId, locationId, InternalLocationType.STOCK);
        InventoryItem quant = itemRepository
                .findByProductIdAndWarehouseIdAndInternalLocationIdAndLotNumberAndSerialNumber(
                        productId, warehouseId, resolvedLocation, normalizedLot, normalizedSerial)
                .orElseThrow(() -> new DomainException(
                        "No existe quant para producto/almacén/ubicación/lote/serie indicados"));
        return apply(quant, -quantity,
                operationType == null ? OperationType.DELIVERY : operationType,
                reason, referenceId, referenceService);
    }

    /**
     * @deprecated Usar {@link #issueNamedLot}. Prohibido repartir entre lotes sin que el llamador
     * nombre cada lote (no FEFO/findFirst silencioso).
     */
    @Deprecated
    @Transactional
    public void issueByExpiry(Long productId, int quantity, MovementType type, String reason,
                              Long referenceId, String referenceService) {
        throw new DomainException(
                "La salida exige warehouseId y lotNumber explícitos; no se reparte entre lotes sin nombrarlos");
    }

    @Transactional
    public void reverseReference(Long referenceId, String referenceService, String reason) {
        List<InventoryMovement> original = movementRepository
                .findByReferenceServiceAndReferenceId(referenceService, referenceId).stream()
                .filter(movement -> !movement.isCancelled())
                .filter(movement -> movement.getReason() == null || !movement.getReason().startsWith("Reverso"))
                .toList();
        for (InventoryMovement movement : original) {
            if (movement.getInventoryItem() == null) {
                continue;
            }
            apply(movement.getInventoryItem(), -movement.getQuantityChange(), movement.getOperationType(),
                    "Reverso: " + reason, referenceId, referenceService);
            movement.markCancelled();
            movementRepository.save(movement);
        }
    }

    private InventoryItem findOrOpenQuant(Long productId, Long warehouseId, Long locationId,
                                          String lotNumber, String serialNumber, Money unitCost) {
        if (warehouseId == null) {
            throw new DomainException("La entrada de stock exige un almacén");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainException("El producto no existe"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("El almacén no existe"));
        String normalizedLot = InventoryItem.normalizeLot(lotNumber);
        String normalizedSerial = InventoryItem.normalizeSerial(serialNumber);
        Long resolvedLocation = resolveLocationId(warehouseId, locationId, InternalLocationType.STOCK);
        Money cost = unitCost == null ? Money.zero(Currency.PEN) : unitCost;
        return itemRepository
                .findByProductIdAndWarehouseIdAndInternalLocationIdAndLotNumberAndSerialNumber(
                        productId, warehouseId, resolvedLocation, normalizedLot, normalizedSerial)
                .orElseGet(() -> InventoryItem.open(
                        product, warehouse, resolvedLocation, normalizedLot, normalizedSerial, cost));
    }

    private InventoryMovement apply(InventoryItem quant, int delta, OperationType operationType, String reason,
                                    Long referenceId, String referenceService) {
        LotTraceabilityPolicy.assertQuantReady(quant);
        StockLot stockLot = resolveLot(quant);
        Long serialId = resolveSerialId(quant, stockLot.getId());
        quant.applyDelta(delta, quantityPolicy.allowsNegative());
        itemRepository.save(quant);
        InventoryMovement movement = InventoryMovement.record(
                quant,
                delta,
                operationType,
                reason,
                referenceId,
                referenceService,
                LocalDateTime.now(clock),
                stockLot.getId(),
                serialId
        );
        return movementRepository.save(movement);
    }

    private StockLot resolveLot(InventoryItem quant) {
        Long productId = quant.getProduct().getId();
        String lotNumber = quant.getLotNumber();
        return stockLotRepository.findByProductIdAndLotNumber(productId, lotNumber)
                .orElseGet(() -> stockLotRepository.save(
                        StockLot.open(productId, lotNumber, quant.getExpirationDate())));
    }

    private Long resolveSerialId(InventoryItem quant, Long stockLotId) {
        if (!quant.isSerialTracked()) {
            return null;
        }
        Long productId = quant.getProduct().getId();
        String serial = quant.getSerialNumber();
        StockSerial existing = stockSerialRepository.findByProductIdAndSerialNumber(productId, serial)
                .orElseGet(() -> stockSerialRepository.save(StockSerial.open(productId, stockLotId, serial)));
        if (existing.getLotId() == null) {
            existing.assignLot(stockLotId);
            stockSerialRepository.save(existing);
        }
        return existing.getId();
    }

    private InventoryItem requireQuant(Long quantId) {
        if (quantId == null) {
            throw new DomainException("El movimiento debe indicar el lote. No se elige un lote al azar");
        }
        return itemRepository.findById(quantId)
                .orElseThrow(() -> new DomainException("El lote no existe"));
    }

    private Long resolveLocationId(Long warehouseId, Long locationId, InternalLocationType fallbackType) {
        if (locationId != null) {
            InternalLocation location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new DomainException("La ubicación interna no existe"));
            if (!location.getWarehouseId().equals(warehouseId)) {
                throw new DomainException("La ubicación no pertenece al almacén");
            }
            return location.getId();
        }
        return locationRepository.findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(warehouseId, fallbackType)
                .map(InternalLocation::getId)
                .orElseGet(() -> locationRepository.save(InternalLocation.defaultOf(warehouseId, fallbackType)).getId());
    }
}
