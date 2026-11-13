package com.valeerp.inventory.cyclecount;

import com.valeerp.inventory.service.model.InventoryBookRefs;
import com.valeerp.inventory.service.model.InventoryItem;
import com.valeerp.inventory.service.repository.InventoryItemRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.OperationType;
import com.valeerp.inventory.StockService;
import com.valeerp.inventory.cyclecount.CycleCountDtos.CreateCycleCountRequest;
import com.valeerp.inventory.cyclecount.CycleCountDtos.CycleCountLineResponse;
import com.valeerp.inventory.cyclecount.CycleCountDtos.CycleCountResponse;
import com.valeerp.inventory.cyclecount.CycleCountDtos.RecordCountRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Conteo cíclico: al completar, genera {@link com.valeerp.inventory.service.model.InventoryMovement}
 * compensatorios (ADJUSTMENT). El libro permanece consultable; no soft-delete de movimientos.
 */
@Service
public class CycleCountService {

    private final CycleCountRepository cycleCountRepository;
    private final StockService stockService;
    private final InventoryItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final Clock clock;

    public CycleCountService(CycleCountRepository cycleCountRepository,
                            StockService stockService,
                            InventoryItemRepository itemRepository,
                            ProductRepository productRepository,
                            WarehouseRepository warehouseRepository,
                            Clock clock) {
        this.cycleCountRepository = cycleCountRepository;
        this.stockService = stockService;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CycleCountResponse> listByCompany(Long companyId) {
        requireCompany(companyId);
        return cycleCountRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CycleCountResponse get(Long id) {
        return toResponse(requireCount(id));
    }

    @Transactional
    public CycleCountResponse create(CreateCycleCountRequest request) {
        requireCompany(request.companyId());
        requireWarehouse(request.warehouseId());
        String code = request.code().trim().toUpperCase();
        if (cycleCountRepository.existsByCompanyIdAndCode(request.companyId(), code)) {
            throw new DomainException("Ya existe un conteo con código " + code);
        }
        CycleCount count = CycleCount.draft(
                request.companyId(),
                code,
                request.warehouseId(),
                request.notes(),
                Actors.current(),
                Instant.now(clock)
        );
        for (var lineReq : request.lines()) {
            requireProduct(lineReq.productId());
            InventoryItem quant = requireQuantInWarehouse(lineReq.inventoryItemId(), request.warehouseId());
            if (!lineReq.productId().equals(quant.getProduct().getId())) {
                throw new DomainException("El quant no pertenece al producto de la línea");
            }
            int systemQty = lineReq.systemQuantity() != null
                    ? lineReq.systemQuantity()
                    : (quant.getQuantity() == null ? 0 : quant.getQuantity());
            count.addLine(lineReq.productId(), quant.getId(), systemQty);
        }
        return toResponse(cycleCountRepository.save(count));
    }

    @Transactional
    public CycleCountResponse start(Long id) {
        CycleCount count = requireCount(id);
        count.start(Instant.now(clock));
        return toResponse(cycleCountRepository.save(count));
    }

    @Transactional
    public CycleCountResponse recordLineCount(Long countId, Long lineId, RecordCountRequest request) {
        CycleCount count = requireCount(countId);
        count.assertEditable();
        CycleCountLine line = count.mutableLines().stream()
                .filter(l -> lineId.equals(l.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("La línea de conteo no existe"));
        line.recordCount(request.countedQuantity());
        return toResponse(cycleCountRepository.save(count));
    }

    @Transactional
    public CycleCountResponse complete(Long id) {
        CycleCount count = requireCount(id);
        String reason = "Ajuste por conteo cíclico " + count.getCode();
        for (CycleCountLine line : count.mutableLines()) {
            line.assertCounted();
            int delta = line.adjustmentDelta();
            if (delta == 0) {
                continue;
            }
            // Movimiento compensatorio nombrando el quant; libro permanece consultable
            stockService.adjustLot(
                    line.getInventoryItemId(),
                    delta,
                    OperationType.ADJUSTMENT,
                    reason,
                    count.getId(),
                    InventoryBookRefs.CYCLE_COUNT
            );
        }
        count.complete(Instant.now(clock));
        return toResponse(cycleCountRepository.save(count));
    }

    @Transactional
    public CycleCountResponse cancel(Long id) {
        CycleCount count = requireCount(id);
        count.cancel(Instant.now(clock));
        return toResponse(cycleCountRepository.save(count));
    }

    private InventoryItem requireQuantInWarehouse(Long quantId, Long warehouseId) {
        InventoryItem quant = itemRepository.findById(quantId)
                .orElseThrow(() -> new DomainException("El quant no existe"));
        if (!warehouseId.equals(quant.getWarehouse().getId())) {
            throw new DomainException("El quant no pertenece al almacén del conteo");
        }
        return quant;
    }

    private CycleCount requireCount(Long id) {
        return cycleCountRepository.findById(id)
                .orElseThrow(() -> new DomainException("El conteo cíclico no existe"));
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

    private CycleCountResponse toResponse(CycleCount count) {
        List<CycleCountLineResponse> lines = count.getLines().stream()
                .map(line -> new CycleCountLineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getInventoryItemId(),
                        line.getSystemQuantity(),
                        line.getCountedQuantity(),
                        line.getCountedQuantity() == null ? null : line.adjustmentDelta()
                ))
                .toList();
        return new CycleCountResponse(
                count.getId(),
                count.getCompanyId(),
                count.getCode(),
                count.getWarehouseId(),
                count.getStatus(),
                count.getNotes(),
                count.getCreatedAt(),
                count.getStartedAt(),
                count.getCompletedAt(),
                count.getCancelledAt(),
                count.getCreatedBy(),
                lines
        );
    }
}
