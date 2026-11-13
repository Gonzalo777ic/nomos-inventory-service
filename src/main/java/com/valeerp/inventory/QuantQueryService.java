package com.valeerp.inventory;

import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.StockDtos.OpenQuantRequest;
import com.valeerp.inventory.StockDtos.QuantResponse;
import com.valeerp.inventory.StockDtos.UpdateQuantRequest;
import com.valeerp.inventory.location.InternalLocation;
import com.valeerp.inventory.location.InternalLocationService;
import com.valeerp.inventory.location.InternalLocationType;
import com.valeerp.inventory.stock.StockLot;
import com.valeerp.inventory.stock.StockLotRepository;
import com.valeerp.inventory.stock.StockSerial;
import com.valeerp.inventory.stock.StockSerialRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Alta/consulta de quants sin mutar cantidad. La cantidad solo cambia en {@link StockService}.
 */
@Service
public class QuantQueryService {

    private final InventoryItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InternalLocationService locationService;
    private final StockLotRepository lotRepository;
    private final StockSerialRepository serialRepository;
    private final Clock clock;

    public QuantQueryService(InventoryItemRepository itemRepository,
                             ProductRepository productRepository,
                             WarehouseRepository warehouseRepository,
                             InternalLocationService locationService,
                             StockLotRepository lotRepository,
                             StockSerialRepository serialRepository,
                             Clock clock) {
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.locationService = locationService;
        this.lotRepository = lotRepository;
        this.serialRepository = serialRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<QuantResponse> listByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new DomainException("El producto no existe");
        }
        return itemRepository.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public int totalStock(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new DomainException("El producto no existe");
        }
        Integer total = itemRepository.calculateTotalStockByProductId(productId);
        return total == null ? 0 : total;
    }

    @Transactional
    public QuantResponse openEmpty(OpenQuantRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new DomainException("El producto no existe"));
        var warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new DomainException("El almacén no existe"));
        locationService.ensureDefaultsForWarehouse(request.warehouseId());
        InternalLocation location = request.internalLocationId() == null
                ? locationService.requireDefault(request.warehouseId(), InternalLocationType.STOCK)
                : locationService.requireOwned(request.warehouseId(), request.internalLocationId());
        String lotNumber = InventoryItem.normalizeLot(request.lotNumber());
        String serial = InventoryItem.normalizeSerial(request.serialNumber());
        if (itemRepository.findByProductIdAndWarehouseIdAndInternalLocationIdAndLotNumberAndSerialNumber(
                request.productId(), request.warehouseId(), location.getId(), lotNumber, serial).isPresent()) {
            throw new DomainException("Ya existe un quant para ese producto/ubicación/lote/serie");
        }
        StockLot lot = lotRepository.findByProductIdAndLotNumber(request.productId(), lotNumber)
                .orElseGet(() -> lotRepository.save(
                        StockLot.open(request.productId(), lotNumber, request.expirationDate())));
        if (request.expirationDate() != null) {
            lot.rescheduleExpiration(request.expirationDate());
            lotRepository.save(lot);
        }
        if (!serial.isBlank()) {
            serialRepository.findByProductIdAndSerialNumber(request.productId(), serial)
                    .orElseGet(() -> serialRepository.save(
                            StockSerial.open(request.productId(), lot.getId(), serial)));
        }
        Money cost = request.unitCost() == null ? Money.zero(Currency.PEN) : request.unitCost();
        InventoryItem quant = InventoryItem.open(product, warehouse, location.getId(), lotNumber, serial, cost);
        quant.rescheduleExpiration(request.expirationDate());
        return toResponse(itemRepository.save(quant));
    }

    @Transactional
    public QuantResponse updateMetadata(Long id, UpdateQuantRequest request) {
        InventoryItem quant = itemRepository.findById(id)
                .orElseThrow(() -> new DomainException("El quant no existe"));
        if (request.unitCost() != null) {
            quant.changeUnitCost(request.unitCost());
        }
        if (request.expirationDate() != null) {
            quant.rescheduleExpiration(request.expirationDate());
        }
        return toResponse(itemRepository.save(quant));
    }

    @Transactional
    public void retireEmpty(Long id) {
        InventoryItem quant = itemRepository.findById(id)
                .orElseThrow(() -> new DomainException("El quant no existe"));
        if (quant.getQuantity() != null && quant.getQuantity() > 0) {
            throw new DomainException("No se da de baja un quant con existencias. Registre un movimiento");
        }
        quant.retire(Actors.current(), Instant.now(clock));
        itemRepository.save(quant);
    }

    QuantResponse toResponse(InventoryItem item) {
        return new QuantResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getSku(),
                item.getWarehouse().getId(),
                item.getInternalLocationId(),
                item.getLotNumber(),
                item.getSerialNumber(),
                item.getQuantity(),
                item.getReservedQuantity(),
                item.availableQuantity(),
                item.getUnitCost(),
                item.getExpirationDate(),
                item.getEntryDate()
        );
    }
}
