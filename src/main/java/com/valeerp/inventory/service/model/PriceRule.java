package com.valeerp.inventory.service.model;

import com.valeerp.catalog.CatalogMoneySupport;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Regla de precio por partner (id local) y/o categoría.
 * Sin Feign: {@code partnerId} es proyección/referencia local.
 */
@Entity
@Table(name = "price_rules")
public class PriceRule extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @Column(nullable = false, length = 160)
    private String name;

    /** Partner comercial local (id/proyección). Null = no filtra por partner. */
    @Column(name = "partner_id")
    private Long partnerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private int priority;

    /** Ajuste porcentual (ej. -0.10 = 10% descuento). Null si hay precio fijo. */
    @Column(name = "percent_adjustment", precision = 9, scale = 6)
    private BigDecimal percentAdjustment;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "fixed_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "fixed_currency", length = 3))
    })
    private Money fixedPrice;

    protected PriceRule() {
    }

    private PriceRule(Long priceListId, String name, Long partnerId, Long categoryId,
                      int priority, BigDecimal percentAdjustment, Money fixedPrice) {
        this.priceListId = priceListId;
        this.name = name;
        this.partnerId = partnerId;
        this.categoryId = categoryId;
        this.priority = priority;
        this.percentAdjustment = percentAdjustment;
        this.fixedPrice = fixedPrice;
    }

    public static PriceRule percent(Long priceListId, String name, Long partnerId, Long categoryId,
                                    int priority, BigDecimal percentAdjustment) {
        requireCommon(priceListId, name, partnerId, categoryId);
        if (percentAdjustment == null) {
            throw new DomainException("El ajuste porcentual es obligatorio");
        }
        return new PriceRule(priceListId, name.trim(), partnerId, categoryId, priority, percentAdjustment, null);
    }

    public static PriceRule fixed(Long priceListId, String name, Long partnerId, Long categoryId,
                                  int priority, Money fixedPrice) {
        requireCommon(priceListId, name, partnerId, categoryId);
        return new PriceRule(priceListId, name.trim(), partnerId, categoryId, priority, null,
                CatalogMoneySupport.requireNonNegative(fixedPrice, "precio fijo"));
    }

    public Money applyTo(Money base) {
        CatalogMoneySupport.requireNonNegative(base, "precio base");
        if (fixedPrice != null) {
            if (!fixedPrice.currency().equals(base.currency())) {
                throw new DomainException("La moneda del precio fijo no coincide con la base");
            }
            return fixedPrice;
        }
        BigDecimal factor = BigDecimal.ONE.add(percentAdjustment);
        if (factor.signum() < 0) {
            throw new DomainException("El ajuste porcentual produce un precio negativo");
        }
        return base.times(factor);
    }

    public boolean matches(Long partnerId, Long categoryId) {
        boolean partnerOk = this.partnerId == null || this.partnerId.equals(partnerId);
        boolean categoryOk = this.categoryId == null || this.categoryId.equals(categoryId);
        return partnerOk && categoryOk;
    }

    public Long getId() {
        return id;
    }

    public Long getPriceListId() {
        return priceListId;
    }

    public String getName() {
        return name;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public int getPriority() {
        return priority;
    }

    public BigDecimal getPercentAdjustment() {
        return percentAdjustment;
    }

    public Money getFixedPrice() {
        return fixedPrice;
    }

    private static void requireCommon(Long priceListId, String name, Long partnerId, Long categoryId) {
        if (priceListId == null) {
            throw new DomainException("priceListId es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la regla es obligatorio");
        }
        if (partnerId == null && categoryId == null) {
            throw new DomainException("La regla exige partnerId y/o categoryId");
        }
    }
}
