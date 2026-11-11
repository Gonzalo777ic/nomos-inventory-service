package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Alerta de inventario (libro operativo). No se borra físicamente: se resuelve o descarta.
 */
@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AlertStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer severity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    protected Alert() {
    }

    public static Alert open(Product product, InventoryItem inventoryItem, AlertType type,
                             String title, String description, int severity, LocalDateTime when) {
        if (product == null) {
            throw new DomainException("La alerta exige un producto");
        }
        if (type == null) {
            throw new DomainException("El tipo de alerta es obligatorio");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("El título de la alerta es obligatorio");
        }
        Alert alert = new Alert();
        alert.product = product;
        alert.inventoryItem = inventoryItem;
        alert.type = type;
        alert.status = AlertStatus.ACTIVE;
        alert.title = title.trim();
        alert.description = description;
        alert.severity = severity;
        alert.createdAt = when == null ? LocalDateTime.now() : when;
        return alert;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = AlertStatus.ACTIVE;
        }
    }

    public void resolve(LocalDateTime when) {
        assertActive();
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = when == null ? LocalDateTime.now() : when;
    }

    public void dismiss(LocalDateTime when) {
        assertActive();
        this.status = AlertStatus.DISMISSED;
        this.resolvedAt = when == null ? LocalDateTime.now() : when;
    }

    private void assertActive() {
        if (this.status != AlertStatus.ACTIVE) {
            throw new DomainException("Solo se cierra una alerta activa");
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public AlertType getType() {
        return type;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSeverity() {
        return severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
