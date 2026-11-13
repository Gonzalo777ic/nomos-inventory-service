package com.valeerp.mrp;

import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.mrp.BillOfMaterials.TheoreticalNeed;
import com.valeerp.mrp.BomDtos.CreateRequest;
import com.valeerp.mrp.BomDtos.ExplosionLineResponse;
import com.valeerp.mrp.BomDtos.ExplosionResponse;
import com.valeerp.mrp.BomDtos.LineRequest;
import com.valeerp.mrp.BomDtos.LineResponse;
import com.valeerp.mrp.BomDtos.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BomService {

    private final BomRepository bomRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public BomService(BomRepository bomRepository, ProductRepository productRepository, Clock clock) {
        this.bomRepository = bomRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Response> listByProduct(Long productId) {
        return bomRepository.findByProductIdOrderByCodeAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return toResponse(requireWithLines(id));
    }

    @Transactional
    public Response create(CreateRequest request) {
        requireProduct(request.productId());
        String code = request.code().trim().toUpperCase();
        if (bomRepository.existsByCode(code)) {
            throw new DomainException("Ya existe una BOM con código " + code);
        }
        int outputQty = request.outputQuantity() == null ? 1 : request.outputQuantity();
        BillOfMaterials bom = BillOfMaterials.draft(code, request.productId(), outputQty, request.notes());
        for (LineRequest line : request.lines()) {
            requireProduct(line.componentProductId());
            if (line.childBomId() != null) {
                BillOfMaterials child = requireWithLines(line.childBomId());
                if (!child.getProductId().equals(line.componentProductId())) {
                    throw new DomainException("El sub-BOM no corresponde al producto componente");
                }
            }
            bom.addComponent(line.componentProductId(), line.quantityPer(), line.childBomId(), line.notes());
        }
        return toResponse(bomRepository.save(bom));
    }

    @Transactional
    public Response activate(Long id) {
        BillOfMaterials bom = requireWithLines(id);
        bom.activate();
        return toResponse(bomRepository.save(bom));
    }

    @Transactional
    public Response archive(Long id) {
        BillOfMaterials bom = requireWithLines(id);
        bom.archive();
        return toResponse(bomRepository.save(bom));
    }

    @Transactional
    public void retire(Long id) {
        BillOfMaterials bom = requireWithLines(id);
        if (bom.getStatus() == BomStatus.ACTIVE) {
            bom.archive();
        }
        bom.retire(Actors.current(), Instant.now(clock));
        bomRepository.save(bom);
    }

    /**
     * Explosión multi-nivel a hojas (y nodos intermedios marcados).
     * No muta stock ni pickings.
     */
    @Transactional(readOnly = true)
    public ExplosionResponse explode(Long bomId, int finishedQuantity) {
        BillOfMaterials root = requireWithLines(bomId);
        if (root.getStatus() != BomStatus.ACTIVE) {
            throw new DomainException("Solo se explota una BOM activa");
        }
        Map<Long, Integer> leafTotals = new LinkedHashMap<>();
        Set<Long> visiting = new HashSet<>();
        explodeRecursive(root, finishedQuantity, visiting, leafTotals);
        List<ExplosionLineResponse> leaves = leafTotals.entrySet().stream()
                .map(e -> new ExplosionLineResponse(e.getKey(), e.getValue(), -1, true))
                .toList();
        return new ExplosionResponse(root.getId(), root.getCode(), root.getProductId(), finishedQuantity, leaves);
    }

    /**
     * Consumo teórico de hojas para una OF. Reutiliza la explosión multi-nivel.
     */
    @Transactional(readOnly = true)
    public List<TheoreticalNeed> theoreticalLeaves(Long bomId, int finishedQuantity) {
        ExplosionResponse explosion = explode(bomId, finishedQuantity);
        return explosion.components().stream()
                .map(line -> new TheoreticalNeed(line.componentProductId(), line.quantity(), null))
                .toList();
    }

    private void explodeRecursive(BillOfMaterials bom,
                                  int finishedQuantity,
                                  Set<Long> visiting,
                                  Map<Long, Integer> leafTotals) {
        if (!visiting.add(bom.getId())) {
            throw new DomainException("Ciclo detectado en BOM " + bom.getCode());
        }
        for (TheoreticalNeed need : bom.theoreticalOneLevel(finishedQuantity)) {
            BillOfMaterials child = resolveChildBom(need);
            if (child != null) {
                explodeRecursive(child, need.quantity(), visiting, leafTotals);
            } else {
                leafTotals.merge(need.componentProductId(), need.quantity(), Integer::sum);
            }
        }
        visiting.remove(bom.getId());
    }

    private BillOfMaterials resolveChildBom(TheoreticalNeed need) {
        if (need.childBomId() != null) {
            return requireWithLines(need.childBomId());
        }
        List<BillOfMaterials> actives = bomRepository.findActiveByProductId(need.componentProductId());
        if (actives.isEmpty()) {
            return null;
        }
        return requireWithLines(actives.get(0).getId());
    }

    BillOfMaterials requireWithLines(Long id) {
        return bomRepository.findByIdWithLines(id)
                .orElseThrow(() -> new DomainException("BOM no encontrada"));
    }

    private void requireProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new DomainException("Producto no encontrado: " + productId);
        }
    }

    private Response toResponse(BillOfMaterials bom) {
        List<LineResponse> lines = bom.getLines().stream()
                .map(line -> new LineResponse(
                        line.getId(),
                        line.getComponentProductId(),
                        line.getQuantityPer(),
                        line.getChildBomId(),
                        line.getNotes()
                ))
                .toList();
        return new Response(
                bom.getId(),
                bom.getCode(),
                bom.getProductId(),
                bom.getOutputQuantity(),
                bom.getStatus(),
                bom.getNotes(),
                lines
        );
    }
}
