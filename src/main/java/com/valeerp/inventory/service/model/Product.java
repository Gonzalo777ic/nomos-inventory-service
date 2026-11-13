package com.valeerp.inventory.service.model;

import com.valeerp.catalog.CatalogMoneySupport;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.catalog.ProductCostingPolicy;
import com.valeerp.catalog.ProductType;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Agregado de catálogo (sin cantidad física).
 * <p>
 * Unicidad de SKU vigente (PostgreSQL):
 * {@code CREATE UNIQUE INDEX uq_products_sku_active ON products (sku) WHERE deleted_at IS NULL;}
 * No hay cascada SQL de borrado hacia variantes, imágenes ni adjuntos: la baja es lógica.
 */
@Entity
@Table(name = "products")
public class Product extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    @Column(name = "brand", nullable = false)
    private Long brandId;

    @Column(name = "category", nullable = false)
    private Long categoryId;

    @Column(name = "unit_of_measure", nullable = false)
    private Long unitOfMeasureId;

    @Column(length = 64)
    private String barcode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money listPrice;

    @Column(name = "min_stock_threshold", nullable = false)
    private Integer minStockThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "costing_method", nullable = false, length = 20)
    private CostingMethod costingMethod;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "standard_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "standard_cost_currency", nullable = false, length = 3))
    })
    private Money standardCost;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "average_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "average_cost_currency", nullable = false, length = 3))
    })
    private Money averageCost;

    protected Product() {
    }

    private Product(String sku, String name, ProductType productType, Long brandId, Long categoryId,
                    Long unitOfMeasureId, String barcode, Money listPrice, Integer minStockThreshold,
                    CostingMethod costingMethod, Money standardCost, Money averageCost) {
        this.sku = sku;
        this.name = name;
        this.productType = productType;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.unitOfMeasureId = unitOfMeasureId;
        this.barcode = barcode;
        this.listPrice = listPrice;
        this.minStockThreshold = minStockThreshold;
        this.costingMethod = costingMethod;
        this.standardCost = standardCost;
        this.averageCost = averageCost;
    }

    public static Product register(String sku, String name, Long brandId, Long categoryId,
                                   Long unitOfMeasureId, String barcode, Money listPrice,
                                   Integer minStockThreshold, CostingMethod costingMethod,
                                   Money standardCost) {
        return register(sku, name, ProductType.STORABLE, brandId, categoryId, unitOfMeasureId,
                barcode, listPrice, minStockThreshold, costingMethod, standardCost);
    }

    public static Product register(String sku, String name, ProductType productType, Long brandId,
                                   Long categoryId, Long unitOfMeasureId, String barcode,
                                   Money listPrice, Integer minStockThreshold,
                                   CostingMethod costingMethod, Money standardCost) {
        requireSku(sku);
        requireName(name);
        if (productType == null) {
            throw new DomainException("El tipo de producto es obligatorio");
        }
        requireId(brandId, "brandId");
        requireId(categoryId, "categoryId");
        requireId(unitOfMeasureId, "unitOfMeasureId");
        Money price = CatalogMoneySupport.requireNonNegative(listPrice, "precio de lista");
        Money std = standardCost == null
                ? Money.zero(price.currency())
                : CatalogMoneySupport.requireNonNegative(standardCost, "coste estándar");
        if (!price.currency().equals(std.currency())) {
            throw new DomainException("Precio y coste deben compartir moneda");
        }
        CostingMethod method = costingMethod == null ? CostingMethod.STANDARD : costingMethod;
        int threshold = productType.tracksInventory()
                ? (minStockThreshold == null ? 0 : minStockThreshold)
                : 0;
        if (threshold < 0) {
            throw new DomainException("El umbral de stock mínimo no puede ser negativo");
        }
        return new Product(
                sku.trim().toUpperCase(),
                name.trim(),
                productType,
                brandId,
                categoryId,
                unitOfMeasureId,
                normalizeBarcode(barcode),
                price,
                threshold,
                method,
                std,
                Money.zero(price.currency())
        );
    }

    /** Alias usado por el API de maestros de catálogo (#23). */
    public static Product publish(String sku, String name, ProductType type, Long brandId,
                                  Money listPrice, Integer minStockThreshold,
                                  Long categoryId, Long unitOfMeasureId) {
        return register(sku, name, type, brandId, categoryId, unitOfMeasureId,
                null, listPrice, minStockThreshold, CostingMethod.STANDARD, Money.zero(listPrice.currency()));
    }

    public void rename(String name) {
        requireName(name);
        this.name = name.trim();
    }

    public void rename(String sku, String name) {
        changeSku(sku);
        rename(name);
    }

    public void changeProductType(ProductType type) {
        reclassify(type);
    }

    public void reclassify(ProductType type) {
        if (type == null) {
            throw new DomainException("El tipo de producto es obligatorio");
        }
        this.productType = type;
        if (!type.tracksInventory()) {
            this.minStockThreshold = 0;
        }
    }

    public void assignCatalogRefs(Long brandId, Long categoryId, Long unitOfMeasureId) {
        reclassify(brandId, categoryId, unitOfMeasureId);
    }

    public void reclassify(Long brandId, Long categoryId, Long unitOfMeasureId) {
        requireId(brandId, "brandId");
        requireId(categoryId, "categoryId");
        requireId(unitOfMeasureId, "unitOfMeasureId");
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.unitOfMeasureId = unitOfMeasureId;
    }

    public void changeSku(String sku) {
        requireSku(sku);
        this.sku = sku.trim().toUpperCase();
    }

    public void changeBarcode(String barcode) {
        this.barcode = normalizeBarcode(barcode);
    }

    public void changeListPrice(Money nextPrice) {
        this.listPrice = CatalogMoneySupport.requireNonNegative(nextPrice, "precio de lista");
    }

    public void changePrice(BigDecimal nextPrice) {
        changeListPrice(CatalogMoneySupport.penOrZero(nextPrice));
    }

    public void setPrice(BigDecimal nextPrice) {
        changePrice(nextPrice);
    }

    public void changeMinStockThreshold(Integer threshold) {
        redefineStockThreshold(threshold);
    }

    public void redefineStockThreshold(Integer threshold) {
        int value = threshold == null ? 0 : threshold;
        if (value < 0) {
            throw new DomainException("El umbral de stock mínimo no puede ser negativo");
        }
        this.minStockThreshold = value;
    }

    public void changeCostingMethod(CostingMethod method) {
        if (method == null) {
            throw new DomainException("El método de coste es obligatorio");
        }
        this.costingMethod = method;
    }

    public void changeStandardCost(Money cost) {
        this.standardCost = CatalogMoneySupport.requireNonNegative(cost, "coste estándar");
    }

    public void applyIncomingCost(Money unitCost, int onHandQtyBefore, int incomingQty) {
        this.averageCost = ProductCostingPolicy.nextAverageCost(
                this.averageCost, onHandQtyBefore, unitCost, incomingQty);
    }

    public Money valuationCost() {
        return ProductCostingPolicy.valuationCost(costingMethod, standardCost, averageCost);
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public ProductType getProductType() {
        return productType;
    }

    public Long getBrandId() {
        return brandId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getUnitOfMeasureId() {
        return unitOfMeasureId;
    }

    public String getBarcode() {
        return barcode;
    }

    public Money getListPrice() {
        return listPrice;
    }

    public BigDecimal getPrice() {
        return listPrice == null ? null : listPrice.amount();
    }

    public Integer getMinStockThreshold() {
        return minStockThreshold;
    }

    public CostingMethod getCostingMethod() {
        return costingMethod;
    }

    public Money getStandardCost() {
        return standardCost;
    }

    public Money getAverageCost() {
        return averageCost;
    }

    private static void requireSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new DomainException("El SKU es obligatorio");
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del producto es obligatorio");
        }
    }

    private static void requireId(Long id, String field) {
        if (id == null) {
            throw new DomainException(field + " es obligatorio");
        }
    }

    private static String normalizeBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }
        return barcode.trim();
    }
}
