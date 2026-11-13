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
 * Sucursal de una compañía. Referencia local {@code companyId} (sin Feign a auth).
 */
@Entity
@Table(name = "branches")
public class Branch extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 255)
    private String address;

    protected Branch() {
    }

    private Branch(Long companyId, String code, String name, String address) {
        this.companyId = companyId;
        this.code = code;
        this.name = name;
        this.address = address;
    }

    public static Branch open(Long companyId, String code, String name, String address) {
        requireCompany(companyId);
        requireCode(code);
        requireName(name);
        return new Branch(companyId, normalizeCode(code), name.trim(), blankToNull(address));
    }

    public void rename(String code, String name, String address) {
        requireCode(code);
        requireName(name);
        this.code = normalizeCode(code);
        this.name = name.trim();
        this.address = blankToNull(address);
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

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    private static void requireCompany(Long companyId) {
        if (companyId == null) {
            throw new DomainException("La sucursal exige companyId");
        }
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de sucursal es obligatorio");
        }
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de sucursal es obligatorio");
        }
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
