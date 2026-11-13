package com.valeerp.inventory.picking;

import com.valeerp.inventory.service.model.PickingStatus;
import com.valeerp.inventory.OperationType;
import com.valeerp.kernel.DomainException;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Transferencia / picking. Estados {@code draft → confirmed → done|cancel} solo vía métodos de dominio.
 */
@Entity
@Table(name = "pickings")
public class Picking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 32)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PickingStatus status;

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "destination_warehouse_id")
    private Long destinationWarehouseId;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @OneToMany(mappedBy = "picking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<PickingLine> lines = new ArrayList<>();

    protected Picking() {
    }

    public static Picking draft(Long companyId, String code, OperationType operationType,
                                Long sourceWarehouseId, Long destinationWarehouseId,
                                String notes, String actor, Instant when) {
        if (companyId == null) {
            throw new DomainException("companyId es obligatorio");
        }
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del picking es obligatorio");
        }
        if (operationType == null) {
            throw new DomainException("El tipo de operación es obligatorio");
        }
        if (sourceWarehouseId == null) {
            throw new DomainException("El almacén origen es obligatorio");
        }
        if (operationType == OperationType.INTERNAL_TRANSFER && destinationWarehouseId == null) {
            throw new DomainException("La transferencia interna exige almacén destino");
        }
        Picking picking = new Picking();
        picking.companyId = companyId;
        picking.code = code.trim().toUpperCase();
        picking.operationType = operationType;
        picking.status = PickingStatus.DRAFT;
        picking.sourceWarehouseId = sourceWarehouseId;
        picking.destinationWarehouseId = destinationWarehouseId;
        picking.notes = notes == null || notes.isBlank() ? null : notes.trim();
        picking.createdAt = when == null ? Instant.now() : when;
        picking.createdBy = actor == null || actor.isBlank() ? "sistema" : actor;
        return picking;
    }

    public PickingLine addLine(Long productId, int quantity, Long inventoryItemId,
                               String lotNumber, String serialNumber,
                               Long sourceLocationId, Long destinationLocationId) {
        assertDraft();
        PickingLine line = PickingLine.open(this, productId, quantity, inventoryItemId,
                lotNumber, serialNumber, sourceLocationId, destinationLocationId);
        lines.add(line);
        return line;
    }

    public void confirm(Instant when) {
        if (status != PickingStatus.DRAFT) {
            throw new DomainException("Solo se confirma un picking en borrador");
        }
        if (lines.isEmpty()) {
            throw new DomainException("El picking exige al menos una línea");
        }
        for (PickingLine line : lines) {
            line.assertReadyToConfirm(operationType);
        }
        this.status = PickingStatus.CONFIRMED;
        this.confirmedAt = when == null ? Instant.now() : when;
    }

    public void complete(Instant when) {
        if (status != PickingStatus.CONFIRMED) {
            throw new DomainException("Solo se completa un picking confirmado");
        }
        this.status = PickingStatus.DONE;
        this.completedAt = when == null ? Instant.now() : when;
    }

    public void cancel(Instant when) {
        if (status == PickingStatus.DONE) {
            throw new DomainException("No se cancela un picking ya completado");
        }
        if (status == PickingStatus.CANCELLED) {
            throw new DomainException("El picking ya está cancelado");
        }
        this.status = PickingStatus.CANCELLED;
        this.cancelledAt = when == null ? Instant.now() : when;
    }

    public void assertDraft() {
        if (status != PickingStatus.DRAFT) {
            throw new DomainException("Solo se edita un picking en borrador");
        }
    }

    public boolean reservesStock() {
        return operationType == OperationType.DELIVERY
                || operationType == OperationType.INTERNAL_TRANSFER
                || operationType == OperationType.SCRAP;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public PickingStatus getStatus() {
        return status;
    }

    public Long getSourceWarehouseId() {
        return sourceWarehouseId;
    }

    public Long getDestinationWarehouseId() {
        return destinationWarehouseId;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<PickingLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    List<PickingLine> mutableLines() {
        return lines;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Picking that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
