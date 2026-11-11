package com.valeerp.inventory.location;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Ubicación interna de un almacén (stock, input, output, scrap). Maestro con soft-delete.
 */
@Entity
@Table(name = "internal_locations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_internal_location_wh_code", columnNames = {"warehouse_id", "code"})
})
public class InternalLocation extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private InternalLocationType locationType;

    @Column(name = "is_default", nullable = false)
    private boolean defaultForType;

    protected InternalLocation() {
    }

    private InternalLocation(Long warehouseId, String code, String name,
                             InternalLocationType locationType, boolean defaultForType) {
        this.warehouseId = warehouseId;
        this.code = code;
        this.name = name;
        this.locationType = locationType;
        this.defaultForType = defaultForType;
    }

    public static InternalLocation open(Long warehouseId, String code, String name,
                                        InternalLocationType locationType, boolean defaultForType) {
        requireWarehouse(warehouseId);
        requireCode(code);
        requireName(name);
        if (locationType == null) {
            throw new DomainException("El tipo de ubicación es obligatorio");
        }
        return new InternalLocation(warehouseId, normalizeCode(code), name.trim(), locationType, defaultForType);
    }

    public static InternalLocation defaultOf(Long warehouseId, InternalLocationType type) {
        String code = type.name();
        return open(warehouseId, code, type.name(), type, true);
    }

    public void rename(String code, String name) {
        requireCode(code);
        requireName(name);
        this.code = normalizeCode(code);
        this.name = name.trim();
    }

    public void markDefault() {
        this.defaultForType = true;
    }

    public void clearDefault() {
        this.defaultForType = false;
    }

    public Long getId() {
        return id;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public InternalLocationType getLocationType() {
        return locationType;
    }

    public boolean isDefaultForType() {
        return defaultForType;
    }

    private static void requireWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new DomainException("warehouseId es obligatorio");
        }
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de ubicación es obligatorio");
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de ubicación es obligatorio");
        }
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
