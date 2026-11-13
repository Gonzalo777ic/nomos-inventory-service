package com.valeerp.inventory;

import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.InventoryMovement;
import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.location.InternalLocation;
import com.valeerp.inventory.location.InternalLocationRepository;
import com.valeerp.inventory.location.InternalLocationType;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reservas de stock para pickings. No escribe on-hand a mano: solo {@link InventoryItem#reserve}
 * / {@link InventoryItem#releaseReservation}; la cantidad física sigue vía {@link StockService}.
 */
@Service
public class StockReservationService {

    private final InventoryItemRepository itemRepository;
    private final InternalLocationRepository locationRepository;
    private final StockService stockService;

    public StockReservationService(InventoryItemRepository itemRepository,
                                   InternalLocationRepository locationRepository,
                                   StockService stockService) {
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.stockService = stockService;
    }

    /**
     * Resuelve un quant nombrado. Si hay varios sin nombre → {@link DomainException}.
     * No usa {@code findFirst()} sobre candidatos múltiples.
     */
    @Transactional(readOnly = true)
    public InventoryItem resolveNamedQuant(Long productId, Long warehouseId, Long locationId,
                                           Long quantId, String lotNumber, String serialNumber) {
        if (productId == null || warehouseId == null) {
            throw new DomainException("productId y warehouseId son obligatorios para resolver el quant");
        }
        if (quantId != null) {
            InventoryItem quant = requireQuant(quantId);
            if (!productId.equals(quant.getProduct().getId())) {
                throw new DomainException("El quant no pertenece al producto " + productId);
            }
            if (!warehouseId.equals(quant.getWarehouse().getId())) {
                throw new DomainException("El quant no pertenece al almacén " + warehouseId);
            }
            return quant;
        }
        Long resolvedLocationId = resolveLocationId(warehouseId, locationId);
        String normalizedSerial = InventoryItem.normalizeSerial(serialNumber);
        if (lotNumber != null && !lotNumber.isBlank()) {
            String normalizedLot = InventoryItem.normalizeLot(lotNumber);
            return itemRepository
                    .findByProductIdAndWarehouseIdAndInternalLocationIdAndLotNumberAndSerialNumber(
                            productId, warehouseId, resolvedLocationId, normalizedLot, normalizedSerial)
                    .orElseThrow(() -> new DomainException(
                            "No existe quant para producto " + productId + " lote " + normalizedLot));
        }
        List<InventoryItem> candidates = itemRepository.findByProductIdAndWarehouseId(productId, warehouseId).stream()
                .filter(q -> resolvedLocationId.equals(q.getInternalLocationId()))
                .filter(q -> q.availableQuantity() > 0)
                .toList();
        if (candidates.isEmpty()) {
            throw new DomainException("No hay stock disponible para el producto " + productId
                    + " en el almacén " + warehouseId);
        }
        if (candidates.size() > 1) {
            throw new DomainException(
                    "Hay varios lotes/quants; nombre inventoryItemId o lotNumber. No se elige un lote al azar");
        }
        return candidates.get(0);
    }

    @Transactional
    public InventoryItem reserveQuant(Long quantId, int quantity) {
        InventoryItem quant = requireQuant(quantId);
        quant.reserve(quantity);
        return itemRepository.save(quant);
    }

    @Transactional
    public InventoryItem releaseQuantReservation(Long quantId, int quantity) {
        InventoryItem quant = requireQuant(quantId);
        quant.releaseReservation(quantity);
        return itemRepository.save(quant);
    }

    @Transactional
    public InventoryMovement completeReservedIssue(Long quantId, int quantity, OperationType operationType,
                                                   String reason, Long referenceId, String referenceService) {
        InventoryItem quant = requireQuant(quantId);
        quant.releaseReservation(quantity);
        itemRepository.save(quant);
        return stockService.adjustLot(
                quantId,
                -quantity,
                operationType == null ? OperationType.DELIVERY : operationType,
                reason,
                referenceId,
                referenceService
        );
    }

    private InventoryItem requireQuant(Long quantId) {
        if (quantId == null) {
            throw new DomainException("El movimiento debe indicar el lote. No se elige un lote al azar");
        }
        return itemRepository.findById(quantId)
                .orElseThrow(() -> new DomainException("El quant no existe"));
    }

    private Long resolveLocationId(Long warehouseId, Long locationId) {
        if (locationId != null) {
            InternalLocation location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new DomainException("La ubicación interna no existe"));
            if (!warehouseId.equals(location.getWarehouseId())) {
                throw new DomainException("La ubicación no pertenece al almacén");
            }
            return location.getId();
        }
        return locationRepository
                .findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(warehouseId, InternalLocationType.STOCK)
                .map(InternalLocation::getId)
                .orElseGet(() -> locationRepository.save(InternalLocation.defaultOf(warehouseId, InternalLocationType.STOCK))
                        .getId());
    }
}
