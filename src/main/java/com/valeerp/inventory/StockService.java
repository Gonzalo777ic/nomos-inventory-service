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
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class StockService {

    private final InventoryItemRepository itemRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public StockService(InventoryItemRepository itemRepository,
                        InventoryMovementRepository movementRepository,
                        ProductRepository productRepository,
                        WarehouseRepository warehouseRepository) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public void restock(Long productId, int quantity, String reason, Long referenceId, String referenceService) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad a reingresar debe ser mayor que cero");
        }
        InventoryItem lot = availableLots(productId).stream().findFirst()
                .or(() -> itemRepository.findByProductId(productId).stream().findFirst())
                .orElseThrow(() -> new DomainException("No hay un lote donde reingresar el producto " + productId));
        apply(lot, quantity, MovementType.AJUSTE_DEVOLUCION, reason, referenceId, referenceService);
    }

    @Transactional
    public void issueByExpiry(Long productId, int quantity, MovementType type, String reason,
                              Long referenceId, String referenceService) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad a descontar debe ser mayor que cero");
        }
        List<InventoryItem> lots = availableLots(productId);
        int available = lots.stream().mapToInt(InventoryItem::getQuantity).sum();
        if (available < quantity) {
            throw new DomainException("Stock insuficiente para el producto " + productId);
        }
        int remaining = quantity;
        for (InventoryItem lot : lots) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(lot.getQuantity(), remaining);
            apply(lot, -take, type, reason, referenceId, referenceService);
            remaining -= take;
        }
    }

    @Transactional
    public void receive(Long productId, Long warehouseId, String lotNumber, int quantity,
                        BigDecimal unitCost, Long referenceId, String referenceService, String reason) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad recibida debe ser mayor que cero");
        }
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new DomainException("La entrada de stock exige un número de lote");
        }
        if (warehouseId == null) {
            throw new DomainException("La entrada de stock exige un almacén");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainException("El producto no existe"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new DomainException("El almacén no existe"));
        InventoryItem lot = itemRepository
                .findByProductIdAndWarehouseIdAndLotNumber(productId, warehouseId, lotNumber)
                .orElseGet(() -> InventoryItem.open(product, warehouse, lotNumber, unitCost));
        apply(lot, quantity, MovementType.ENTRADA, reason, referenceId, referenceService);
    }

    @Transactional
    public InventoryMovement adjustLot(Long lotId, int delta, MovementType type, String reason,
                                       Long referenceId, String referenceService) {
        if (lotId == null) {
            throw new DomainException("El movimiento debe indicar el lote. No se elige un lote al azar");
        }
        if (delta == 0) {
            throw new DomainException("El movimiento no puede ser cero");
        }
        InventoryItem lot = itemRepository.findById(lotId)
                .orElseThrow(() -> new DomainException("El lote no existe"));
        return apply(lot, delta, type, reason, referenceId, referenceService);
    }

    @Transactional
    public void reverseReference(Long referenceId, String referenceService, String reason) {
        List<InventoryMovement> original = movementRepository
                .findByReferenceServiceAndReferenceId(referenceService, referenceId).stream()
                .filter(movement -> movement.getReason() == null || !movement.getReason().startsWith("Reverso"))
                .toList();
        for (InventoryMovement movement : original) {
            if (movement.getInventoryItem() == null) {
                continue;
            }
            apply(movement.getInventoryItem(), -movement.getQuantityChange(), movement.getType(),
                    "Reverso: " + reason, referenceId, referenceService);
        }
    }

    private List<InventoryItem> availableLots(Long productId) {
        return itemRepository.findByProductId(productId).stream()
                .filter(lot -> lot.getQuantity() != null && lot.getQuantity() > 0)
                .sorted(Comparator
                        .comparing(InventoryItem::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(InventoryItem::getEntryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private InventoryMovement apply(InventoryItem lot, int delta, MovementType type, String reason,
                                    Long referenceId, String referenceService) {
        lot.applyDelta(delta);
        itemRepository.save(lot);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(lot);
        movement.setProduct(lot.getProduct());
        movement.setQuantityChange(delta);
        movement.setBalanceAfter(lot.getQuantity());
        movement.setWarehouseId(lot.getWarehouse().getId());
        movement.setType(type);
        movement.setReason(reason);
        movement.setMovementDate(LocalDateTime.now());
        movement.setReferenceId(referenceId);
        movement.setReferenceService(referenceService);
        return movementRepository.save(movement);
    }
}
