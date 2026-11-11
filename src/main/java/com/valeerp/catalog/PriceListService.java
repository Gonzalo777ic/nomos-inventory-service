package com.valeerp.catalog;

import com.nomos.inventory.service.model.PriceList;
import com.nomos.inventory.service.model.PriceListItem;
import com.nomos.inventory.service.model.PriceRule;
import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.CategoryRepository;
import com.nomos.inventory.service.repository.PriceListItemRepository;
import com.nomos.inventory.service.repository.PriceListRepository;
import com.nomos.inventory.service.repository.PriceRuleRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.valeerp.catalog.PriceListDtos.CreatePriceListRequest;
import com.valeerp.catalog.PriceListDtos.CreatePriceRuleRequest;
import com.valeerp.catalog.PriceListDtos.PriceListItemResponse;
import com.valeerp.catalog.PriceListDtos.PriceListResponse;
import com.valeerp.catalog.PriceListDtos.PriceRuleResponse;
import com.valeerp.catalog.PriceListDtos.ResolvePriceRequest;
import com.valeerp.catalog.PriceListDtos.ResolvedPriceResponse;
import com.valeerp.catalog.PriceListDtos.UpsertPriceListItemRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PriceListService {

    private final PriceListRepository priceListRepository;
    private final PriceListItemRepository itemRepository;
    private final PriceRuleRepository ruleRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public PriceListService(PriceListRepository priceListRepository,
                            PriceListItemRepository itemRepository,
                            PriceRuleRepository ruleRepository,
                            ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            Clock clock) {
        this.priceListRepository = priceListRepository;
        this.itemRepository = itemRepository;
        this.ruleRepository = ruleRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PriceListResponse> listAll() {
        return priceListRepository.findAll().stream().map(this::toListResponse).toList();
    }

    @Transactional(readOnly = true)
    public PriceListResponse get(Long id) {
        return toListResponse(requireList(id));
    }

    @Transactional
    public PriceListResponse create(CreatePriceListRequest request) {
        String code = request.code().trim().toUpperCase();
        if (priceListRepository.existsByCode(code)) {
            throw new DomainException("Ya existe una lista con código " + code);
        }
        PriceList list = PriceList.open(code, request.name(), request.kind(), request.currencyCode());
        return toListResponse(priceListRepository.save(list));
    }

    @Transactional
    public void retire(Long id) {
        PriceList list = requireList(id);
        list.retire(Actors.current(), Instant.now(clock));
        priceListRepository.save(list);
    }

    @Transactional(readOnly = true)
    public List<PriceListItemResponse> listItems(Long priceListId) {
        requireList(priceListId);
        return itemRepository.findByPriceListIdOrderByProductIdAsc(priceListId).stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Transactional
    public PriceListItemResponse upsertItem(Long priceListId, UpsertPriceListItemRequest request) {
        PriceList list = requireList(priceListId);
        if (!productRepository.existsById(request.productId())) {
            throw new DomainException("Producto no encontrado: " + request.productId());
        }
        Money price = CatalogMoneySupport.ofAmount(
                request.amount(),
                request.currency() == null ? list.getCurrencyCode() : request.currency());
        if (!price.currencyCode().equals(list.getCurrencyCode())) {
            throw new DomainException("La moneda del ítem debe ser " + list.getCurrencyCode());
        }
        Optional<PriceListItem> existing = request.variantId() == null
                ? itemRepository.findByPriceListIdAndProductIdAndVariantIdIsNull(priceListId, request.productId())
                : itemRepository.findByPriceListIdAndProductIdAndVariantId(
                priceListId, request.productId(), request.variantId());
        PriceListItem item = existing.orElseGet(() ->
                PriceListItem.of(priceListId, request.productId(), request.variantId(), price));
        item.changePrice(price);
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<PriceRuleResponse> listRules(Long priceListId) {
        requireList(priceListId);
        return ruleRepository.findByPriceListIdOrderByPriorityAscIdAsc(priceListId).stream()
                .map(this::toRuleResponse)
                .toList();
    }

    @Transactional
    public PriceRuleResponse createRule(Long priceListId, CreatePriceRuleRequest request) {
        requireList(priceListId);
        if (request.partnerId() == null && request.categoryId() == null) {
            throw new DomainException("La regla exige partnerId y/o categoryId");
        }
        if (request.categoryId() != null && !categoryRepository.existsById(request.categoryId())) {
            throw new DomainException("Categoría no encontrada: " + request.categoryId());
        }
        // partnerId es proyección local: no se valida contra otro servicio.
        PriceRule rule;
        if (request.fixedAmount() != null) {
            rule = PriceRule.fixed(
                    priceListId,
                    request.name(),
                    request.partnerId(),
                    request.categoryId(),
                    request.priority(),
                    CatalogMoneySupport.ofAmount(request.fixedAmount(), request.fixedCurrency())
            );
        } else if (request.percentAdjustment() != null) {
            rule = PriceRule.percent(
                    priceListId,
                    request.name(),
                    request.partnerId(),
                    request.categoryId(),
                    request.priority(),
                    request.percentAdjustment()
            );
        } else {
            throw new DomainException("Indica percentAdjustment o fixedAmount");
        }
        return toRuleResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void retireRule(Long priceListId, Long ruleId) {
        PriceRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new DomainException("Regla no encontrada: " + ruleId));
        if (!rule.getPriceListId().equals(priceListId)) {
            throw new DomainException("La regla no pertenece a la lista " + priceListId);
        }
        rule.retire(Actors.current(), Instant.now(clock));
        ruleRepository.save(rule);
    }

    /**
     * Resuelve precio: regla (partner/categoría) → ítem de lista → precio de lista del producto.
     */
    @Transactional(readOnly = true)
    public ResolvedPriceResponse resolve(ResolvePriceRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new DomainException("Producto no encontrado: " + request.productId()));
        PriceList list = resolveList(request);
        Money base = findItemPrice(list.getId(), request.productId(), request.variantId())
                .orElse(product.getListPrice());
        Optional<PriceRule> matched = ruleRepository.findByPriceListIdOrderByPriorityAscIdAsc(list.getId()).stream()
                .filter(rule -> rule.matches(request.partnerId(), product.getCategoryId()))
                .min(Comparator.comparingInt(PriceRule::getPriority).thenComparing(PriceRule::getId));
        if (matched.isPresent()) {
            PriceRule rule = matched.get();
            return new ResolvedPriceResponse(
                    product.getId(),
                    request.variantId(),
                    list.getId(),
                    rule.getId(),
                    moneyView(rule.applyTo(base)),
                    "RULE"
            );
        }
        String source = findItemPrice(list.getId(), request.productId(), request.variantId()).isPresent()
                ? "PRICE_LIST_ITEM"
                : "PRODUCT_LIST_PRICE";
        return new ResolvedPriceResponse(
                product.getId(),
                request.variantId(),
                list.getId(),
                null,
                moneyView(base),
                source
        );
    }

    private static ProductCatalogDtos.MoneyView moneyView(Money money) {
        if (money == null) {
            return null;
        }
        return new ProductCatalogDtos.MoneyView(money.amount(), money.currencyCode());
    }

    private Optional<Money> findItemPrice(Long priceListId, Long productId, Long variantId) {
        Optional<PriceListItem> item = variantId == null
                ? itemRepository.findByPriceListIdAndProductIdAndVariantIdIsNull(priceListId, productId)
                : itemRepository.findByPriceListIdAndProductIdAndVariantId(priceListId, productId, variantId);
        return item.map(PriceListItem::getPrice);
    }

    private PriceList resolveList(ResolvePriceRequest request) {
        if (request.priceListId() != null) {
            return requireList(request.priceListId());
        }
        PriceListKind kind = request.kind() == null ? PriceListKind.STANDARD : request.kind();
        return priceListRepository.findFirstByKindOrderByIdAsc(kind)
                .orElseThrow(() -> new DomainException("No hay lista de precios de tipo " + kind));
    }

    private PriceList requireList(Long id) {
        return priceListRepository.findById(id)
                .orElseThrow(() -> new DomainException("Lista de precios no encontrada: " + id));
    }

    private PriceListResponse toListResponse(PriceList list) {
        return new PriceListResponse(list.getId(), list.getCode(), list.getName(), list.getKind(), list.getCurrencyCode());
    }

    private PriceListItemResponse toItemResponse(PriceListItem item) {
        return new PriceListItemResponse(
                item.getId(),
                item.getPriceListId(),
                item.getProductId(),
                item.getVariantId(),
                moneyView(item.getPrice())
        );
    }

    private PriceRuleResponse toRuleResponse(PriceRule rule) {
        return new PriceRuleResponse(
                rule.getId(),
                rule.getPriceListId(),
                rule.getName(),
                rule.getPartnerId(),
                rule.getCategoryId(),
                rule.getPriority(),
                rule.getPercentAdjustment(),
                moneyView(rule.getFixedPrice())
        );
    }
}
