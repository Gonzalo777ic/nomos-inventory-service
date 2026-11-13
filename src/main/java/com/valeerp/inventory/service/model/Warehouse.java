package com.valeerp.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Almacén perteneciente a una compañía (y opcionalmente a una sucursal).
 * {@code companyId} / {@code branchId} son referencias locales; no hay llamadas a auth.
 */
@Entity
@Table(name = "warehouses")
public class Warehouse extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "location_address", nullable = false, length = 255)
    private String locationAddress;

    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    protected Warehouse() {
    }

    private Warehouse(Long companyId, Long branchId, String code, String name,
                      String locationAddress, boolean isMain) {
        this.companyId = companyId;
        this.branchId = branchId;
        this.code = code;
        this.name = name;
        this.locationAddress = locationAddress;
        this.isMain = isMain;
    }

    public static Warehouse open(Long companyId, Long branchId, String code, String name,
                                 String locationAddress, boolean isMain) {
        requireCompany(companyId);
        requireCode(code);
        requireName(name);
        requireAddress(locationAddress);
        return new Warehouse(companyId, branchId, normalizeCode(code), name.trim(),
                locationAddress.trim(), isMain);
    }

    public void relocate(Long branchId, String code, String name, String locationAddress, boolean isMain) {
        requireCode(code);
        requireName(name);
        requireAddress(locationAddress);
        this.branchId = branchId;
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.locationAddress = locationAddress.trim();
        this.isMain = isMain;
    }

    public void demoteFromMain() {
        this.isMain = false;
    }

    public void promoteAsMain() {
        this.isMain = true;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public boolean isMain() {
        return isMain;
    }

    /** Compatibilidad con código legado que usaba {@code getIsMain()}. */
    public Boolean getIsMain() {
        return isMain;
    }

    private static void requireCompany(Long companyId) {
        if (companyId == null) {
            throw new DomainException("El almacén exige companyId");
        }
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de almacén es obligatorio");
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del almacén es obligatorio");
        }
    }

    private static void requireAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new DomainException("La dirección del almacén es obligatoria");
        }
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
