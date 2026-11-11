package com.valeerp.inventory.reorder;

import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.reorder.ReorderRuleDtos.CreateReorderRuleRequest;
import com.valeerp.inventory.reorder.ReorderRuleDtos.ReplenishmentSuggestionResponse;
import com.valeerp.inventory.reorder.ReorderRuleDtos.ReorderRuleResponse;
import com.valeerp.inventory.reorder.ReorderRuleDtos.UpdateReorderRuleRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReorderRuleService {

    private final ReorderRuleRepository ruleRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository itemRepository;
    private final Clock clock;

    public ReorderRuleService(ReorderRuleRepository ruleRepository,
                              ProductRepository productRepository,
                              WarehouseRepository warehouseRepository,
                              InventoryItemRepository itemRepository,
                              Clock clock) {
        this.ruleRepository = ruleRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.itemRepository = itemRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReorderRuleResponse> listByWarehouse(Long warehouseId) {
        requireWarehouse(warehouseId);
        return ruleRepository.findByWarehouseIdOrderByProductIdAsc(warehouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReorderRuleResponse get(Long id) {
        return toResponse(requireRule(id));
    }

    @Transactional
    public ReorderRuleResponse create(CreateReorderRuleRequest request) {
        requireProduct(request.productId());
        requireWarehouse(request.warehouseId());
        if (ruleRepository.existsByProductIdAndWarehouseId(request.productId(), request.warehouseId())) {
            throw new DomainException("Ya existe una regla de reabastecimiento para ese producto y almacén");
        }
        ReorderRule rule = ReorderRule.define(
                request.productId(), request.warehouseId(), request.minQuantity(), request.maxQuantity());
        return toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public ReorderRuleResponse update(Long id, UpdateReorderRuleRequest request) {
        ReorderRule rule = requireRule(id);
        rule.redefine(request.minQuantity(), request.maxQuantity());
        return toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void retire(Long id) {
        ReorderRule rule = requireRule(id);
        rule.retire(Actors.current(), Instant.now(clock));
        ruleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentSuggestionResponse> suggestionsForWarehouse(Long warehouseId) {
        requireWarehouse(warehouseId);
        List<ReplenishmentSuggestionResponse> suggestions = new ArrayList<>();
        for (ReorderRule rule : ruleRepository.findByWarehouseIdOrderByProductIdAsc(warehouseId)) {
            int onHand = itemRepository.findByProductIdAndWarehouseId(rule.getProductId(), warehouseId).stream()
                    .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                    .sum();
            int suggested = rule.suggestedReplenishment(onHand);
            if (suggested > 0) {
                suggestions.add(new ReplenishmentSuggestionResponse(
                        rule.getId(),
                        rule.getProductId(),
                        rule.getWarehouseId(),
                        onHand,
                        rule.getMinQuantity(),
                        rule.getMaxQuantity(),
                        suggested
                ));
            }
        }
        return suggestions;
    }

    private ReorderRule requireRule(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new DomainException("La regla de reabastecimiento no existe"));
    }

    private void requireProduct(Long productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new DomainException("El producto no existe");
        }
    }

    private void requireWarehouse(Long warehouseId) {
        if (warehouseId == null || !warehouseRepository.existsById(warehouseId)) {
            throw new DomainException("El almacén no existe");
        }
    }

    private ReorderRuleResponse toResponse(ReorderRule rule) {
        return new ReorderRuleResponse(
                rule.getId(),
                rule.getProductId(),
                rule.getWarehouseId(),
                rule.getMinQuantity(),
                rule.getMaxQuantity()
        );
    }
}
