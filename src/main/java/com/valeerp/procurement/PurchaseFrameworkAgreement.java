package com.valeerp.procurement;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Acuerdo marco / contrato de compra (maestro). Soft-delete vía {@link SoftDeletable}.
 * Precios vigentes por periodo en líneas hijas ({@link Money}).
 */
@Entity
@Table(name = "purchase_framework_agreements")
public class PurchaseFrameworkAgreement extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FrameworkAgreementStatus status;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FrameworkAgreementLine> lines = new ArrayList<>();

    protected PurchaseFrameworkAgreement() {
    }

    public static PurchaseFrameworkAgreement draft(String code, Long supplierId,
                                                   LocalDate validFrom, LocalDate validTo, String notes) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del acuerdo es obligatorio");
        }
        if (supplierId == null) {
            throw new DomainException("El acuerdo exige proveedor");
        }
        requirePeriod(validFrom, validTo);
        PurchaseFrameworkAgreement agreement = new PurchaseFrameworkAgreement();
        agreement.code = code.trim().toUpperCase();
        agreement.supplierId = supplierId;
        agreement.validFrom = validFrom;
        agreement.validTo = validTo;
        agreement.notes = notes;
        agreement.status = FrameworkAgreementStatus.DRAFT;
        return agreement;
    }

    public void addPrice(Long productId, Money unitPrice, LocalDate priceFrom, LocalDate priceTo) {
        assertEditable();
        LocalDate from = priceFrom == null ? validFrom : priceFrom;
        LocalDate to = priceTo == null ? validTo : priceTo;
        if (from.isBefore(validFrom) || to.isAfter(validTo)) {
            throw new DomainException("El precio debe caer dentro del periodo del acuerdo");
        }
        FrameworkAgreementLine line = FrameworkAgreementLine.of(productId, unitPrice, from, to);
        line.attach(this);
        lines.add(line);
    }

    public void activate() {
        if (status != FrameworkAgreementStatus.DRAFT && status != FrameworkAgreementStatus.CLOSED) {
            throw new DomainException("Solo se activa un acuerdo en borrador o cerrado");
        }
        if (lines.isEmpty()) {
            throw new DomainException("El acuerdo no tiene precios");
        }
        this.status = FrameworkAgreementStatus.ACTIVE;
    }

    public void close() {
        if (status != FrameworkAgreementStatus.ACTIVE) {
            throw new DomainException("Solo se cierra un acuerdo activo");
        }
        this.status = FrameworkAgreementStatus.CLOSED;
    }

    public Optional<Money> priceOn(Long productId, LocalDate onDate) {
        if (productId == null || onDate == null) {
            return Optional.empty();
        }
        if (status != FrameworkAgreementStatus.ACTIVE) {
            return Optional.empty();
        }
        if (onDate.isBefore(validFrom) || onDate.isAfter(validTo)) {
            return Optional.empty();
        }
        return lines.stream()
                .filter(line -> productId.equals(line.getProductId()) && line.covers(onDate))
                .map(FrameworkAgreementLine::getUnitPrice)
                .findFirst();
    }

    private void assertEditable() {
        if (status != FrameworkAgreementStatus.DRAFT) {
            throw new DomainException("Solo se editan precios en borrador");
        }
    }

    private static void requirePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new DomainException("El periodo del acuerdo es obligatorio");
        }
        if (to.isBefore(from)) {
            throw new DomainException("validTo no puede ser anterior a validFrom");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public FrameworkAgreementStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public List<FrameworkAgreementLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}
