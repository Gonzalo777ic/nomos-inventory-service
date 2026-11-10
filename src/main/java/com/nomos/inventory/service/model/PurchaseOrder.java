package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore; 
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.valeerp.kernel.DomainException;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList; 
import java.util.List;


@Entity
@Data
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

    @DecimalMin(value = "0.0", inclusive = true, message = "El monto total no puede ser negativo")
    private Double totalAmount;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference 
    private List<PurchaseOrderDetail> details = new ArrayList<>();

    public PurchaseOrder() {

        this.status = OrderStatus.PENDIENTE;
        this.totalAmount = 0.0;
    }

    public void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (details != null) {
            for (PurchaseOrderDetail detail : details) {
                BigDecimal line = BigDecimal.valueOf(detail.getUnitCost())
                        .multiply(BigDecimal.valueOf(detail.getQuantity()));
                total = total.add(line);
            }
        }
        this.totalAmount = total.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public void assertEditable() {
        if (status == OrderStatus.CANCELADO || status == OrderStatus.COMPLETO) {
            throw new DomainException("No se puede editar una orden finalizada");
        }
    }

    public void requestTransition(OrderStatus target, boolean supplierActor) {
        if (target == OrderStatus.COMPLETO) {
            throw new DomainException("La orden se completa al recepcionar la mercadería");
        }
        if (supplierActor) {
            if (status != OrderStatus.PENDIENTE) {
                throw new DomainException("El proveedor solo responde órdenes pendientes");
            }
            if (target != OrderStatus.CONFIRMADO && target != OrderStatus.RECHAZADO) {
                throw new DomainException("El proveedor solo puede confirmar o rechazar");
            }
        } else if (target == OrderStatus.CANCELADO && status == OrderStatus.COMPLETO) {
            throw new DomainException("No se puede cancelar una orden ya recibida");
        }
        this.status = target;
    }

    public void markReceived() {
        if (status != OrderStatus.CONFIRMADO) {
            throw new DomainException("Solo se recepciona una orden confirmada");
        }
        this.status = OrderStatus.COMPLETO;
    }

    public void cancelOpen() {
        if (status != OrderStatus.BORRADOR && status != OrderStatus.PENDIENTE) {
            throw new DomainException("Solo se anula una orden en borrador o pendiente");
        }
        this.status = OrderStatus.CANCELADO;
    }
}