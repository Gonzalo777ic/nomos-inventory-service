package com.valeerp.procurement;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
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

/**
 * Devolución a proveedor (libro). Confirmación genera movimiento compensatorio;
 * anulación confirmada = reverso de stock, sin delete.
 */
@Entity
@Table(name = "vendor_returns")
public class VendorReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private VendorReturnStatus status;

    @OneToMany(mappedBy = "vendorReturn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VendorReturnLine> lines = new ArrayList<>();

    protected VendorReturn() {
    }

    public static VendorReturn draft(Long purchaseOrderId, Long supplierId, LocalDate returnDate, String reason) {
        if (purchaseOrderId == null || supplierId == null || returnDate == null) {
            throw new DomainException("La devolución exige OC, proveedor y fecha");
        }
        VendorReturn vendorReturn = new VendorReturn();
        vendorReturn.purchaseOrderId = purchaseOrderId;
        vendorReturn.supplierId = supplierId;
        vendorReturn.returnDate = returnDate;
        vendorReturn.reason = reason;
        vendorReturn.status = VendorReturnStatus.BORRADOR;
        vendorReturn.lines = new ArrayList<>();
        return vendorReturn;
    }

    public void addLine(Long productId, Long lotId, int quantity, Money unitCost) {
        assertDraft();
        if (lotId == null) {
            throw new DomainException("La devolución exige lote nombrado");
        }
        lines.add(new VendorReturnLine(this, productId, lotId, quantity, unitCost));
    }

    public void confirm() {
        assertDraft();
        if (lines.isEmpty()) {
            throw new DomainException("La devolución no tiene líneas");
        }
        this.status = VendorReturnStatus.CONFIRMADA;
    }

    public void cancel() {
        if (status == VendorReturnStatus.ANULADA) {
            throw new DomainException("La devolución ya está anulada");
        }
        if (status != VendorReturnStatus.BORRADOR && status != VendorReturnStatus.CONFIRMADA) {
            throw new DomainException("Estado de devolución no anulable");
        }
        this.status = VendorReturnStatus.ANULADA;
    }

    public boolean isConfirmed() {
        return status == VendorReturnStatus.CONFIRMADA;
    }

    public Money getTotalAmount() {
        Money total = Money.zero(Currency.PEN);
        for (VendorReturnLine line : lines) {
            total = total.plus(line.getUnitCost().times(line.getQuantity()));
        }
        return total;
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

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getReason() {
        return reason;
    }

    public VendorReturnStatus getStatus() {
        return status;
    }

    public List<VendorReturnLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void assertDraft() {
        if (status != VendorReturnStatus.BORRADOR) {
            throw new DomainException("Solo se edita una devolución en borrador");
        }
    }
}
