package com.valeerp.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID de la Orden de Compra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonBackReference
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "El ID del Producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "La cantidad es obligatoria")
    private Integer quantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity = 0;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", nullable = false, length = 3))
    })
    @NotNull(message = "El costo unitario es obligatorio")
    private Money unitCost;

    /** Lead time de abastecimiento de la línea (días). */
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    /** Promesa de entrega de la línea. */
    @Column(name = "promised_delivery_date")
    private LocalDate promisedDeliveryDate;

    public int residualQuantity() {
        int ordered = quantity == null ? 0 : quantity;
        int received = receivedQuantity == null ? 0 : receivedQuantity;
        return Math.max(0, ordered - received);
    }

    public boolean isFullyReceived() {
        return residualQuantity() == 0 && quantity != null && quantity > 0;
    }

    public void applyReceipt(int qty) {
        if (qty <= 0) {
            throw new DomainException("La cantidad recibida debe ser mayor que cero");
        }
        if (qty > residualQuantity()) {
            throw new DomainException("La recepción supera la cantidad residual de la línea");
        }
        int current = receivedQuantity == null ? 0 : receivedQuantity;
        this.receivedQuantity = current + qty;
    }
}
