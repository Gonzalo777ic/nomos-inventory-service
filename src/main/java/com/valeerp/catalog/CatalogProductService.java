package com.valeerp.catalog;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.ProductVariant;
import com.nomos.inventory.service.model.ProductVariantAttribute;
import com.nomos.inventory.service.repository.BrandRepository;
import com.nomos.inventory.service.repository.CategoryRepository;
import com.nomos.inventory.service.repository.ProductAttributeRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.ProductVariantRepository;
import com.nomos.inventory.service.repository.UnitOfMeasureRepository;
import com.valeerp.catalog.CatalogProductDtos.AttributeValueDto;
import com.valeerp.catalog.CatalogProductDtos.CreateProductRequest;
import com.valeerp.catalog.CatalogProductDtos.CreateVariantRequest;
import com.valeerp.catalog.CatalogProductDtos.MoneyDto;
import com.valeerp.catalog.CatalogProductDtos.ProductResponse;
import com.valeerp.catalog.CatalogProductDtos.UpdateProductRequest;
import com.valeerp.catalog.CatalogProductDtos.UpdateVariantRequest;
import com.valeerp.catalog.CatalogProductDtos.VariantResponse;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Catálogo: productos (tipos Odoo) y variantes con SKU. Sin mutar stock físico.
 */
@Service
public class CatalogProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductAttributeRepository attributeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final Clock clock;

    public CatalogProductService(ProductRepository productRepository,
                                 ProductVariantRepository variantRepository,
                                 ProductAttributeRepository attributeRepository,
                                 BrandRepository brandRepository,
                                 CategoryRepository categoryRepository,
                                 UnitOfMeasureRepository unitOfMeasureRepository,
                                 Clock clock) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.attributeRepository = attributeRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(requireProduct(id));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        assertSkuFree(sku, null, null);
        assertBarcodeFree(request.barcode(), null, null);
        requireMasters(request.brandId(), request.categoryId(), request.unitOfMeasureId());
        ProductType type = ProductType.parse(request.productType());
        Money listPrice = CatalogMoneySupport.ofAmount(request.listPriceAmount(), request.listPriceCurrency());
        Money standard = request.standardCostAmount() == null
                ? Money.zero(listPrice.currency())
                : CatalogMoneySupport.ofAmount(
                request.standardCostAmount(),
                request.standardCostCurrency() == null ? listPrice.currencyCode() : request.standardCostCurrency());
        Product product = Product.register(
                sku, request.name(), type, request.brandId(), request.categoryId(), request.unitOfMeasureId(),
                request.barcode(), listPrice, request.minStockThreshold(), request.costingMethod(), standard);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = requireProduct(id);
        String sku = request.sku().trim().toUpperCase();
        assertSkuFree(sku, id, null);
        assertBarcodeFree(request.barcode(), id, null);
        requireMasters(request.brandId(), request.categoryId(), request.unitOfMeasureId());
        product.rename(sku, request.name());
        product.changeProductType(ProductType.parse(request.productType()));
        product.changeBarcode(request.barcode());
        product.assignCatalogRefs(request.brandId(), request.categoryId(), request.unitOfMeasureId());
        product.changeListPrice(CatalogMoneySupport.ofAmount(request.listPriceAmount(), request.listPriceCurrency()));
        product.changeMinStockThreshold(request.minStockThreshold());
        if (request.costingMethod() != null) {
            product.changeCostingMethod(request.costingMethod());
        }
        if (request.standardCostAmount() != null) {
            product.changeStandardCost(CatalogMoneySupport.ofAmount(
                    request.standardCostAmount(),
                    request.standardCostCurrency() == null
                            ? product.getListPrice().currencyCode()
                            : request.standardCostCurrency()));
        }
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse applyIncomingCost(Long id, Money unitCost, int onHandQtyBefore, int incomingQty) {
        Product product = requireProduct(id);
        product.applyIncomingCost(unitCost, onHandQtyBefore, incomingQty);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void retire(Long id) {
        Product product = requireProduct(id);
        Instant when = Instant.now(clock);
        String actor = Actors.current();
        for (ProductVariant variant : variantRepository.findByProductIdOrderBySkuAsc(id)) {
            if (!variant.isRetired()) {
                variant.retire(actor, when);
                variantRepository.save(variant);
            }
        }
        product.retire(actor, when);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<VariantResponse> listVariants(Long productId) {
        requireProduct(productId);
        return variantRepository.findByProductIdOrderBySkuAsc(productId).stream()
                .map(this::toVariantResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VariantResponse getVariant(Long variantId) {
        return toVariantResponse(requireVariantById(variantId));
    }

    @Transactional
    public VariantResponse createVariant(Long productId, CreateVariantRequest request) {
        requireProduct(productId);
        String sku = request.sku().trim().toUpperCase();
        assertSkuFree(sku, null, null);
        assertBarcodeFree(request.barcode(), null, null);
        ProductVariant variant = ProductVariant.open(productId, sku, request.barcode(), request.label());
        applyAttributes(variant, request.attributes());
        return toVariantResponse(variantRepository.save(variant));
    }

    @Transactional
    public VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        ProductVariant variant = requireVariant(productId, variantId);
        String sku = request.sku().trim().toUpperCase();
        assertSkuFree(sku, null, variantId);
        assertBarcodeFree(request.barcode(), null, variantId);
        variant.rename(sku, request.label());
        variant.changeBarcode(request.barcode());
        applyAttributes(variant, request.attributes());
        return toVariantResponse(variantRepository.save(variant));
    }

    @Transactional
    public void retireVariant(Long productId, Long variantId) {
        ProductVariant variant = requireVariant(productId, variantId);
        variant.retire(Actors.current(), Instant.now(clock));
        variantRepository.save(variant);
    }

    private void applyAttributes(ProductVariant variant, List<AttributeValueDto> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new DomainException("La variante exige al menos un atributo (p. ej. talla/color)");
        }
        Set<Long> seen = new HashSet<>();
        List<ProductVariantAttribute> mapped = new ArrayList<>();
        for (AttributeValueDto dto : attributes) {
            if (!seen.add(dto.attributeId())) {
                throw new DomainException("Atributo duplicado en la variante: " + dto.attributeId());
            }
            if (!attributeRepository.existsById(dto.attributeId())) {
                throw new DomainException("El atributo " + dto.attributeId() + " no existe");
            }
            mapped.add(ProductVariantAttribute.of(dto.attributeId(), dto.value()));
        }
        variant.replaceAttributes(mapped);
    }

    private void requireMasters(Long brandId, Long categoryId, Long unitOfMeasureId) {
        if (!brandRepository.existsById(brandId)) {
            throw new DomainException("Marca no encontrada: " + brandId);
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new DomainException("Categoría no encontrada: " + categoryId);
        }
        if (!unitOfMeasureRepository.existsById(unitOfMeasureId)) {
            throw new DomainException("Unidad de medida no encontrada: " + unitOfMeasureId);
        }
    }

    private void assertSkuFree(String sku, Long productId, Long variantId) {
        productRepository.findBySku(sku).ifPresent(existing -> {
            if (productId == null || !existing.getId().equals(productId)) {
                throw new DomainException("El SKU " + sku + " ya está registrado");
            }
        });
        variantRepository.findBySku(sku).ifPresent(existing -> {
            if (variantId == null || !existing.getId().equals(variantId)) {
                throw new DomainException("El SKU " + sku + " ya está registrado");
            }
        });
    }

    private void assertBarcodeFree(String barcode, Long productId, Long variantId) {
        if (barcode == null || barcode.isBlank()) {
            return;
        }
        String normalized = barcode.trim();
        productRepository.findByBarcode(normalized).ifPresent(existing -> {
            if (productId == null || !existing.getId().equals(productId)) {
                throw new DomainException("El código de barras ya está en el producto " + existing.getSku());
            }
        });
        variantRepository.findByBarcode(normalized).ifPresent(existing -> {
            if (variantId == null || !existing.getId().equals(variantId)) {
                throw new DomainException("El código de barras ya está en la variante " + existing.getSku());
            }
        });
    }

    private Product requireProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new DomainException("Producto no encontrado: " + id));
    }

    private ProductVariant requireVariant(Long productId, Long variantId) {
        ProductVariant variant = requireVariantById(variantId);
        if (!variant.getProductId().equals(productId)) {
            throw new DomainException("La variante no pertenece al producto " + productId);
        }
        return variant;
    }

    private ProductVariant requireVariantById(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new DomainException("Variante no encontrada: " + variantId));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getProductType().name(),
                product.getBarcode(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getUnitOfMeasureId(),
                moneyDto(product.getListPrice()),
                product.getMinStockThreshold(),
                product.getCostingMethod(),
                moneyDto(product.getStandardCost()),
                moneyDto(product.getAverageCost()),
                moneyDto(product.valuationCost())
        );
    }

    private VariantResponse toVariantResponse(ProductVariant variant) {
        List<AttributeValueDto> attributes = variant.getAttributes().stream()
                .map(item -> new AttributeValueDto(item.getAttributeId(), item.getValue()))
                .toList();
        return new VariantResponse(
                variant.getId(),
                variant.getProductId(),
                variant.getSku(),
                variant.getBarcode(),
                variant.getLabel(),
                attributes
        );
    }

    private static MoneyDto moneyDto(Money money) {
        if (money == null) {
            return null;
        }
        return new MoneyDto(money.amount(), money.currencyCode());
    }
}
