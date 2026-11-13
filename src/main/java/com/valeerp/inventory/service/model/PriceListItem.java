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

/**
 * Precio de un producto (o variante) dentro de una lista.
 */
@Entity
@Table(name = "price_list_items")
public class PriceListItem extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_list_id", nullable = false)
    private Long priceListId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "currency_code", nullable = false, length = 3))
    })
    private Money price;

    protected PriceListItem() {
    }

    private PriceListItem(Long priceListId, Long productId, Long variantId, Money price) {
        this.priceListId = priceListId;
        this.productId = productId;
        this.variantId = variantId;
        this.price = price;
    }

    public static PriceListItem of(Long priceListId, Long productId, Long variantId, Money price) {
        if (priceListId == null) {
            throw new DomainException("priceListId es obligatorio");
        }
        if (productId == null) {
            throw new DomainException("productId es obligatorio");
        }
        return new PriceListItem(priceListId, productId, variantId,
                CatalogMoneySupport.requireNonNegative(price, "precio"));
    }

    public void changePrice(Money price) {
        this.price = CatalogMoneySupport.requireNonNegative(price, "precio");
    }

    public Long getId() {
        return id;
    }

    public Long getPriceListId() {
        return priceListId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Money getPrice() {
        return price;
    }
}
