package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotations")
@Data
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
    @Column(name = "status", nullable = false)
    private QuotationStatus status;

    @Column(name = "total_estimated")
    private Double totalEstimated;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationDetail> details = new ArrayList<>();

    public PurchaseOrder toPurchaseOrder(int leadDays) {
        if (this.status == QuotationStatus.CONVERTIDO) {
            throw new DomainException("Esta cotización ya fue convertida");
        }
        boolean unmapped = details.stream().anyMatch(detail -> detail.getProduct() == null);
        if (unmapped) {
            throw new DomainException("No se puede convertir: hay productos sugeridos sin catalogar");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setOrderDate(LocalDate.now());
        order.setDeliveryDate(LocalDate.now().plusDays(leadDays));
        order.setStatus(OrderStatus.BORRADOR);
        order.setDetails(new ArrayList<>());
        for (QuotationDetail detail : details) {
            PurchaseOrderDetail line = new PurchaseOrderDetail();
            line.setPurchaseOrder(order);
            line.setProduct(detail.getProduct());
            line.setQuantity(detail.getQuantity());
            line.setUnitCost(detail.getQuotedPrice() != null ? detail.getQuotedPrice() : 0.0);
            order.getDetails().add(line);
        }
        order.recalculateTotal();
        this.status = QuotationStatus.CONVERTIDO;
        return order;
    }
}