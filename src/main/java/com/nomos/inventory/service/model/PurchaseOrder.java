package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotNull(message = "La fecha de la orden es obligatoria")
    @PastOrPresent(message = "La fecha de la orden no puede ser futura")
    private LocalDate orderDate;

    @NotNull(message = "La fecha de entrega esperada es obligatoria")
    private LocalDate deliveryDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "total_amount_currency", length = 3))
    })
    @Setter(AccessLevel.NONE)
    private Money totalAmount;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.NONE)
    private OrderStatus status;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    public PurchaseOrder() {
        this.status = OrderStatus.BORRADOR;
        this.totalAmount = Money.zero(Currency.PEN);
    }

    public static PurchaseOrder draft(Supplier supplier, LocalDate orderDate, LocalDate deliveryDate) {
        PurchaseOrder order = new PurchaseOrder();
        order.supplier = supplier;
        order.orderDate = orderDate;
        order.deliveryDate = deliveryDate;
        order.status = OrderStatus.BORRADOR;
        order.totalAmount = Money.zero(Currency.PEN);
        order.details = new ArrayList<>();
        return order;
    }

    public void recalculateTotal() {
        Money total = Money.zero(Currency.PEN);
        if (details != null) {
            for (PurchaseOrderDetail detail : details) {
                if (detail.getUnitCost() == null || detail.getQuantity() == null) {
                    continue;
                }
                total = total.plus(detail.getUnitCost().times(detail.getQuantity()));
            }
        }
        this.totalAmount = total;
    }

    public void assertEditable() {
        if (status != OrderStatus.BORRADOR && status != OrderStatus.PENDIENTE_APROBACION) {
            throw new DomainException("Solo se edita una orden en borrador o pendiente de aprobación");
        }
    }

    /**
     * Envía la OC. Si el total supera el umbral de política, exige aprobación;
     * si no, queda pendiente de respuesta del proveedor.
     */
    public void submit(Money approvalThreshold) {
        if (status != OrderStatus.BORRADOR) {
            throw new DomainException("Solo se envía una orden en borrador");
        }
        if (details == null || details.isEmpty()) {
            throw new DomainException("La orden no tiene líneas");
        }
        recalculateTotal();
        if (approvalThreshold == null) {
            throw new DomainException("El umbral de aprobación es obligatorio");
        }
        if (totalAmount.isGreaterThan(approvalThreshold)) {
            this.status = OrderStatus.PENDIENTE_APROBACION;
        } else {
            this.status = OrderStatus.PENDIENTE;
        }
    }

    public void approve() {
        if (status != OrderStatus.PENDIENTE_APROBACION) {
            throw new DomainException("Solo se aprueba una orden pendiente de aprobación");
        }
        this.status = OrderStatus.PENDIENTE;
    }

    public void rejectApproval() {
        if (status != OrderStatus.PENDIENTE_APROBACION) {
            throw new DomainException("Solo se rechaza la aprobación de una orden pendiente");
        }
        this.status = OrderStatus.RECHAZADO;
    }

    public void requestTransition(OrderStatus target, boolean supplierActor) {
        if (target == OrderStatus.COMPLETO || target == OrderStatus.PARCIAL) {
            throw new DomainException("La orden se completa al recepcionar la mercadería");
        }
        if (supplierActor) {
            if (status != OrderStatus.PENDIENTE) {
                throw new DomainException("El proveedor solo responde órdenes pendientes");
            }
            if (target != OrderStatus.CONFIRMADO && target != OrderStatus.RECHAZADO) {
                throw new DomainException("El proveedor solo puede confirmar o rechazar");
            }
        } else if (target == OrderStatus.CANCELADO && (status == OrderStatus.COMPLETO || status == OrderStatus.PARCIAL)) {
            throw new DomainException("No se puede cancelar una orden ya recibida; use devolución");
        }
        this.status = target;
    }

    /**
     * Aplica recepción parcial o total por línea (detalleId → cantidad).
     * Completar no es un setStatus externo: el estado sale del residual.
     */
    public List<PurchaseOrderDetail> receive(Map<Long, Integer> quantitiesByDetailId) {
        if (status != OrderStatus.CONFIRMADO && status != OrderStatus.PARCIAL) {
            throw new DomainException("Solo se recepciona una orden confirmada o parcialmente recibida");
        }
        if (quantitiesByDetailId == null || quantitiesByDetailId.isEmpty()) {
            throw new DomainException("La recepción exige al menos una línea");
        }
        List<PurchaseOrderDetail> touched = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantitiesByDetailId.entrySet()) {
            PurchaseOrderDetail detail = requireDetail(entry.getKey());
            detail.applyReceipt(entry.getValue());
            touched.add(detail);
        }
        refreshReceiptStatus();
        return touched;
    }

    public void receiveAllResidual() {
        if (status != OrderStatus.CONFIRMADO && status != OrderStatus.PARCIAL) {
            throw new DomainException("Solo se recepciona una orden confirmada o parcialmente recibida");
        }
        if (details == null || details.isEmpty()) {
            throw new DomainException("La orden no tiene líneas para recepcionar");
        }
        boolean any = false;
        for (PurchaseOrderDetail detail : details) {
            int residual = detail.residualQuantity();
            if (residual > 0) {
                detail.applyReceipt(residual);
                any = true;
            }
        }
        if (!any) {
            throw new DomainException("No queda cantidad residual por recepcionar");
        }
        refreshReceiptStatus();
    }

    /** @deprecated prefer {@link #receive(Map)} / {@link #receiveAllResidual()} */
    public void markReceived() {
        receiveAllResidual();
    }

    public void cancelOpen() {
        if (status != OrderStatus.BORRADOR
                && status != OrderStatus.PENDIENTE
                && status != OrderStatus.PENDIENTE_APROBACION) {
            throw new DomainException("Solo se anula una orden en borrador, pendiente o pendiente de aprobación");
        }
        this.status = OrderStatus.CANCELADO;
    }

    public int totalResidualQuantity() {
        if (details == null) {
            return 0;
        }
        return details.stream().mapToInt(PurchaseOrderDetail::residualQuantity).sum();
    }

    private void refreshReceiptStatus() {
        boolean anyReceived = details.stream()
                .anyMatch(d -> d.getReceivedQuantity() != null && d.getReceivedQuantity() > 0);
        boolean allDone = details.stream().allMatch(PurchaseOrderDetail::isFullyReceived);
        if (allDone) {
            this.status = OrderStatus.COMPLETO;
        } else if (anyReceived) {
            this.status = OrderStatus.PARCIAL;
        }
    }

    private PurchaseOrderDetail requireDetail(Long detailId) {
        return details.stream()
                .filter(d -> d.getId() != null && d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new DomainException("La línea " + detailId + " no pertenece a la orden"));
    }
}
