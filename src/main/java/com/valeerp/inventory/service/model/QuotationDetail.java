package com.valeerp.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Línea de RFQ/cotización (libro). No se borra físicamente fuera del agregado;
 * la cotización se cancela por estado.
 */
@Entity
@Table(name = "quotation_details")
public class QuotationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "quoted_price", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "quoted_price_currency", length = 3))
    })
    private Money quotedPrice;

    @Column(name = "sku_suggestion", length = 64)
    private String skuSuggestion;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "promised_delivery_date")
    private LocalDate promisedDeliveryDate;

    protected QuotationDetail() {
    }

    static QuotationDetail ofCatalog(Quotation quotation, Product product, int quantity, Money quotedPrice) {
        if (product == null) {
            throw new DomainException("La línea de catálogo exige producto");
        }
        QuotationDetail line = base(quotation, product.getName(), quantity, quotedPrice);
        line.product = product;
        return line;
    }

    static QuotationDetail ofSuggestion(Quotation quotation, String productName, String skuSuggestion,
                                        int quantity, Money quotedPrice) {
        if (productName == null || productName.isBlank()) {
            throw new DomainException("El nombre del producto sugerido es obligatorio");
        }
        QuotationDetail line = base(quotation, productName.trim(), quantity, quotedPrice);
        line.skuSuggestion = skuSuggestion == null || skuSuggestion.isBlank()
                ? null
                : skuSuggestion.trim().toUpperCase();
        return line;
    }

    private static QuotationDetail base(Quotation quotation, String productName, int quantity, Money quotedPrice) {
        if (quotation == null) {
            throw new DomainException("La línea exige cotización");
        }
        if (quantity <= 0) {
            throw new DomainException("La cantidad de la línea debe ser mayor que cero");
        }
        QuotationDetail line = new QuotationDetail();
        line.quotation = quotation;
        line.productName = productName;
        line.quantity = quantity;
        line.quotedPrice = quotedPrice;
        return line;
    }

    void linkProduct(Product product) {
        if (product == null) {
            throw new DomainException("El producto es obligatorio");
        }
        this.product = product;
        this.productName = product.getName();
    }

    void requote(Money quotedPrice) {
        this.quotedPrice = quotedPrice;
    }

    void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("La cantidad de la línea debe ser mayor que cero");
        }
        this.quantity = quantity;
    }

    void schedulePromise(Integer leadTimeDays, LocalDate promisedDeliveryDate) {
        if (leadTimeDays != null && leadTimeDays < 0) {
            throw new DomainException("El lead time de línea no puede ser negativo");
        }
        this.leadTimeDays = leadTimeDays;
        this.promisedDeliveryDate = promisedDeliveryDate;
    }

    public Long getId() {
        return id;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Money getQuotedPrice() {
        return quotedPrice;
    }

    public String getSkuSuggestion() {
        return skuSuggestion;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public LocalDate getPromisedDeliveryDate() {
        return promisedDeliveryDate;
    }
}
