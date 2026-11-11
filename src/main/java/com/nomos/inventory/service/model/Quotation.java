package com.nomos.inventory.service.model;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RFQ / cotización a proveedor. Máquina de estados en el agregado.
 * Soft-delete solo aplica al maestro {@link Supplier}; aquí se cancela por estado.
 */
@Entity
@Table(name = "quotations")
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private QuotationStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_estimated", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "total_estimated_currency", length = 3))
    })
    private Money totalEstimated;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationDetail> details = new ArrayList<>();

    protected Quotation() {
    }

    public static Quotation draft(Supplier supplier, LocalDate requestDate, LocalDate expirationDate, String notes) {
        if (supplier == null) {
            throw new DomainException("La cotización exige proveedor");
        }
        if (supplier.isRetired()) {
            throw new DomainException("No se cotiza con un proveedor dado de baja");
        }
        Quotation quotation = new Quotation();
        quotation.supplier = supplier;
        quotation.requestDate = requestDate == null ? LocalDate.now() : requestDate;
        quotation.expirationDate = expirationDate;
        quotation.status = QuotationStatus.BORRADOR;
        quotation.notes = notes;
        quotation.totalEstimated = Money.zero(Currency.PEN);
        return quotation;
    }

    public QuotationDetail addCatalogLine(Product product, int quantity, Money quotedPrice) {
        return addCatalogLine(product, quantity, quotedPrice, null, null);
    }

    public QuotationDetail addCatalogLine(Product product, int quantity, Money quotedPrice,
                                          Integer leadTimeDays, LocalDate promisedDeliveryDate) {
        assertEditable();
        QuotationDetail line = QuotationDetail.ofCatalog(this, product, quantity, quotedPrice);
        line.schedulePromise(leadTimeDays, promisedDeliveryDate);
        details.add(line);
        recalculateTotal();
        return line;
    }

    public QuotationDetail addSuggestedLine(String productName, String skuSuggestion, int quantity, Money quotedPrice) {
        return addSuggestedLine(productName, skuSuggestion, quantity, quotedPrice, null, null);
    }

    public QuotationDetail addSuggestedLine(String productName, String skuSuggestion, int quantity, Money quotedPrice,
                                            Integer leadTimeDays, LocalDate promisedDeliveryDate) {
        assertEditable();
        QuotationDetail line = QuotationDetail.ofSuggestion(this, productName, skuSuggestion, quantity, quotedPrice);
        line.schedulePromise(leadTimeDays, promisedDeliveryDate);
        details.add(line);
        recalculateTotal();
        return line;
    }

    public void replaceNotes(String notes) {
        assertEditable();
        this.notes = notes;
    }

    public void clearLines() {
        assertEditable();
        details.clear();
        recalculateTotal();
    }

    public void linkLineProduct(Long detailId, Product product) {
        assertEditable();
        QuotationDetail line = requireLine(detailId);
        line.linkProduct(product);
    }

    public void send(LocalDate sentOn) {
        transition(QuotationStatus.ENVIADO);
        this.requestDate = sentOn == null ? LocalDate.now() : sentOn;
    }

    public void cancel() {
        if (status == QuotationStatus.CONVERTIDO) {
            throw new DomainException("No se cancela una cotización ya convertida a OC");
        }
        if (status == QuotationStatus.CANCELADO) {
            throw new DomainException("La cotización ya está cancelada");
        }
        this.status = QuotationStatus.CANCELADO;
    }

    public void markResponded() {
        transition(QuotationStatus.RESPONDIDO);
    }

    public void accept() {
        transition(QuotationStatus.APROBADO);
    }

    public void reject() {
        transition(QuotationStatus.RECHAZADO);
    }

    public PurchaseOrder convertToPurchaseOrder(int leadDays, LocalDate today) {
        if (status != QuotationStatus.APROBADO && status != QuotationStatus.RESPONDIDO) {
            throw new DomainException("Solo se convierte una cotización respondida o aprobada");
        }
        if (details.isEmpty()) {
            throw new DomainException("La cotización no tiene líneas");
        }
        boolean unmapped = details.stream().anyMatch(detail -> detail.getProduct() == null);
        if (unmapped) {
            throw new DomainException("No se puede convertir: hay productos sugeridos sin catalogar");
        }
        LocalDate base = today == null ? LocalDate.now() : today;
        int defaultLead = Math.max(leadDays, 0);
        LocalDate headerPromise = base.plusDays(defaultLead);
        PurchaseOrder order = PurchaseOrder.draft(supplier, base, headerPromise);
        for (QuotationDetail detail : details) {
            int lineLead = detail.getLeadTimeDays() != null ? detail.getLeadTimeDays() : defaultLead;
            if (lineLead < 0) {
                throw new DomainException("El lead time de línea no puede ser negativo");
            }
            LocalDate promise = detail.getPromisedDeliveryDate() != null
                    ? detail.getPromisedDeliveryDate()
                    : base.plusDays(lineLead);
            if (promise.isAfter(headerPromise)) {
                headerPromise = promise;
            }
            PurchaseOrderDetail line = new PurchaseOrderDetail();
            line.setPurchaseOrder(order);
            line.setProduct(detail.getProduct());
            line.setQuantity(detail.getQuantity());
            line.setReceivedQuantity(0);
            line.setUnitCost(detail.getQuotedPrice() != null
                    ? detail.getQuotedPrice()
                    : Money.zero(Currency.PEN));
            line.setLeadTimeDays(lineLead);
            line.setPromisedDeliveryDate(promise);
            order.getDetails().add(line);
        }
        order.setDeliveryDate(headerPromise);
        order.recalculateTotal();
        this.status = QuotationStatus.CONVERTIDO;
        return order;
    }

    /** Compatibilidad con controladores legados. */
    public PurchaseOrder toPurchaseOrder(int leadDays) {
        if (status == QuotationStatus.BORRADOR) {
            send(LocalDate.now());
            markResponded();
            accept();
        } else if (status == QuotationStatus.ENVIADO) {
            markResponded();
            accept();
        } else if (status == QuotationStatus.RESPONDIDO) {
            accept();
        }
        return convertToPurchaseOrder(leadDays, LocalDate.now());
    }

    private void transition(QuotationStatus target) {
        boolean allowed = switch (status) {
            case BORRADOR -> target == QuotationStatus.ENVIADO || target == QuotationStatus.CANCELADO;
            case ENVIADO -> target == QuotationStatus.RESPONDIDO
                    || target == QuotationStatus.CANCELADO
                    || target == QuotationStatus.RECHAZADO;
            case RESPONDIDO -> target == QuotationStatus.APROBADO
                    || target == QuotationStatus.RECHAZADO
                    || target == QuotationStatus.CANCELADO;
            case APROBADO -> target == QuotationStatus.CONVERTIDO || target == QuotationStatus.CANCELADO;
            case CANCELADO, CONVERTIDO, RECHAZADO -> false;
        };
        if (!allowed) {
            throw new DomainException("Transición inválida de " + status + " a " + target);
        }
        this.status = target;
    }

    private void assertEditable() {
        if (status != QuotationStatus.BORRADOR) {
            throw new DomainException("Solo se editan cotizaciones en borrador");
        }
    }

    private QuotationDetail requireLine(Long detailId) {
        return details.stream()
                .filter(d -> d.getId() != null && d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Línea de cotización no encontrada"));
    }

    void recalculateTotal() {
        Money total = Money.zero(Currency.PEN);
        for (QuotationDetail detail : details) {
            if (detail.getQuotedPrice() == null) {
                continue;
            }
            total = total.plus(detail.getQuotedPrice().times(detail.getQuantity()));
        }
        this.totalEstimated = total;
    }

    public Long getId() {
        return id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public QuotationStatus getStatus() {
        return status;
    }

    public Money getTotalEstimated() {
        return totalEstimated;
    }

    public String getNotes() {
        return notes;
    }

    public List<QuotationDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }
}
