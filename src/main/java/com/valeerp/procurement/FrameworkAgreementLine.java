package com.valeerp.procurement;

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

@Entity
@Table(name = "purchase_framework_agreement_lines")
public class FrameworkAgreementLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agreement_id", nullable = false)
    private PurchaseFrameworkAgreement agreement;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_price_currency", nullable = false, length = 3))
    })
    private Money unitPrice;

    @Column(name = "price_valid_from", nullable = false)
    private LocalDate priceValidFrom;

    @Column(name = "price_valid_to", nullable = false)
    private LocalDate priceValidTo;

    protected FrameworkAgreementLine() {
    }

    static FrameworkAgreementLine of(Long productId, Money unitPrice, LocalDate from, LocalDate to) {
        if (productId == null) {
            throw new DomainException("La línea del acuerdo exige producto");
        }
        if (unitPrice == null) {
            throw new DomainException("El precio del acuerdo es obligatorio");
        }
        if (from == null || to == null) {
            throw new DomainException("El periodo de vigencia del precio es obligatorio");
        }
        if (to.isBefore(from)) {
            throw new DomainException("priceValidTo no puede ser anterior a priceValidFrom");
        }
        FrameworkAgreementLine line = new FrameworkAgreementLine();
        line.productId = productId;
        line.unitPrice = unitPrice;
        line.priceValidFrom = from;
        line.priceValidTo = to;
        return line;
    }

    void attach(PurchaseFrameworkAgreement agreement) {
        this.agreement = agreement;
    }

    boolean covers(LocalDate onDate) {
        return !onDate.isBefore(priceValidFrom) && !onDate.isAfter(priceValidTo);
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public LocalDate getPriceValidFrom() {
        return priceValidFrom;
    }

    public LocalDate getPriceValidTo() {
        return priceValidTo;
    }
}
