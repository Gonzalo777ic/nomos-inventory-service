package com.valeerp.procurement;

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

/**
 * Factura de proveedor (libro). Anulación por estado; sin delete físico.
 */
@Entity
@Table(name = "vendor_bills")
public class VendorBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "document_number", nullable = false, length = 60)
    private String documentNumber;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private VendorBillStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "untaxed_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "untaxed_currency", length = 3))
    })
    private Money untaxedAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "tax_currency", length = 3))
    })
    private Money taxAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "total_currency", length = 3))
    })
    private Money totalAmount;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendorBillLine> lines = new ArrayList<>();

    @Column(name = "accounting_stub_ref", length = 80)
    private String accountingStubRef;

    protected VendorBill() {
    }

    public static VendorBill draft(Long supplierId, String documentNumber, LocalDate billDate, Long purchaseOrderId) {
        if (supplierId == null) {
            throw new DomainException("La factura exige proveedor");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new DomainException("La factura exige número de documento");
        }
        if (billDate == null) {
            throw new DomainException("La factura exige fecha");
        }
        VendorBill bill = new VendorBill();
        bill.supplierId = supplierId;
        bill.documentNumber = documentNumber.trim();
        bill.billDate = billDate;
        bill.purchaseOrderId = purchaseOrderId;
        bill.status = VendorBillStatus.BORRADOR;
        bill.untaxedAmount = Money.zero(Currency.PEN);
        bill.taxAmount = Money.zero(Currency.PEN);
        bill.totalAmount = Money.zero(Currency.PEN);
        bill.lines = new ArrayList<>();
        return bill;
    }

    public void addLine(Long productId, String description, int quantity, Money unitPrice) {
        assertDraft();
        VendorBillLine line = new VendorBillLine(this, productId, description, quantity, unitPrice);
        lines.add(line);
        recalculate();
    }

    public void clearLines() {
        assertDraft();
        lines.clear();
        recalculate();
    }

    public void applyTax(Money tax) {
        assertDraft();
        if (tax == null || tax.isNegative()) {
            throw new DomainException("El impuesto no es válido");
        }
        this.taxAmount = tax;
        this.totalAmount = untaxedAmount.plus(tax);
    }

    public void post(VendorBillAccountingPort accountingPort) {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("No se contabiliza una factura sin líneas");
        }
        recalculate();
        this.status = VendorBillStatus.CONTABILIZADA;
        if (accountingPort != null) {
            this.accountingStubRef = accountingPort.proposeEntry(this);
        }
    }

    public void markPaid() {
        if (status != VendorBillStatus.CONTABILIZADA) {
            throw new DomainException("Solo se marca pagada una factura contabilizada");
        }
        this.status = VendorBillStatus.PAGADA;
    }

    public void cancel() {
        if (status == VendorBillStatus.PAGADA) {
            throw new DomainException("No se anula una factura pagada; use nota de crédito");
        }
        if (status == VendorBillStatus.ANULADA) {
            throw new DomainException("La factura ya está anulada");
        }
        this.status = VendorBillStatus.ANULADA;
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public VendorBillStatus getStatus() {
        return status;
    }

    public Money getUntaxedAmount() {
        return untaxedAmount;
    }

    public Money getTaxAmount() {
        return taxAmount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public String getAccountingStubRef() {
        return accountingStubRef;
    }

    public List<VendorBillLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void assertDraft() {
        if (status != VendorBillStatus.BORRADOR) {
            throw new DomainException("Solo se edita una factura en borrador");
        }
    }

    private void recalculate() {
        Money untaxed = Money.zero(Currency.PEN);
        for (VendorBillLine line : lines) {
            untaxed = untaxed.plus(line.lineTotal());
        }
        this.untaxedAmount = untaxed;
        if (this.taxAmount == null) {
            this.taxAmount = Money.zero(Currency.PEN);
        }
        this.totalAmount = untaxed.plus(taxAmount);
    }
}
