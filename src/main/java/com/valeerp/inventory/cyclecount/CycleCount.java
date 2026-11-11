package com.valeerp.inventory.cyclecount;

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
 * Inventario cíclico / conteo. El ajuste se documenta con movimientos compensatorios del libro.
 */
@Entity
@Table(name = "cycle_counts")
public class CycleCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleCountStatus status;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @OneToMany(mappedBy = "cycleCount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<CycleCountLine> lines = new ArrayList<>();

    protected CycleCount() {
    }

    public static CycleCount draft(Long companyId, String code, Long warehouseId,
                                   String notes, String actor, Instant when) {
        if (companyId == null) {
            throw new DomainException("companyId es obligatorio");
        }
        if (code == null || code.isBlank()) {
            throw new DomainException("El código del conteo es obligatorio");
        }
        if (warehouseId == null) {
            throw new DomainException("warehouseId es obligatorio");
        }
        CycleCount count = new CycleCount();
        count.companyId = companyId;
        count.code = code.trim().toUpperCase();
        count.warehouseId = warehouseId;
        count.status = CycleCountStatus.DRAFT;
        count.notes = notes == null || notes.isBlank() ? null : notes.trim();
        count.createdAt = when == null ? Instant.now() : when;
        count.createdBy = actor == null || actor.isBlank() ? "sistema" : actor;
        return count;
    }

    public CycleCountLine addLine(Long productId, Long inventoryItemId, int systemQuantity) {
        assertEditable();
        CycleCountLine line = CycleCountLine.open(this, productId, inventoryItemId, systemQuantity);
        lines.add(line);
        return line;
    }

    public void start(Instant when) {
        if (status != CycleCountStatus.DRAFT) {
            throw new DomainException("Solo se inicia un conteo en borrador");
        }
        if (lines.isEmpty()) {
            throw new DomainException("El conteo exige al menos una línea");
        }
        this.status = CycleCountStatus.IN_PROGRESS;
        this.startedAt = when == null ? Instant.now() : when;
    }

    public void complete(Instant when) {
        if (status != CycleCountStatus.IN_PROGRESS) {
            throw new DomainException("Solo se completa un conteo en progreso");
        }
        for (CycleCountLine line : lines) {
            line.assertCounted();
        }
        this.status = CycleCountStatus.DONE;
        this.completedAt = when == null ? Instant.now() : when;
    }

    public void cancel(Instant when) {
        if (status == CycleCountStatus.DONE) {
            throw new DomainException("No se cancela un conteo ya aplicado");
        }
        if (status == CycleCountStatus.CANCELLED) {
            throw new DomainException("El conteo ya está cancelado");
        }
        this.status = CycleCountStatus.CANCELLED;
        this.cancelledAt = when == null ? Instant.now() : when;
    }

    public void assertEditable() {
        if (status != CycleCountStatus.DRAFT && status != CycleCountStatus.IN_PROGRESS) {
            throw new DomainException("El conteo no admite cambios en estado " + status);
        }
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

    public Long getWarehouseId() {
        return warehouseId;
    }

    public CycleCountStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
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

    public List<CycleCountLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    List<CycleCountLine> mutableLines() {
        return lines;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CycleCount that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
