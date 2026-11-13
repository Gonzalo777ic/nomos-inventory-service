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
 * Marca de catálogo. Unicidad vigente de código:
 * {@code CREATE UNIQUE INDEX uq_brands_code_active ON brands (code) WHERE deleted_at IS NULL;}
 */
@Entity
@Table(name = "brands")
public class Brand extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(length = 255)
    private String website;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    protected Brand() {
    }

    private Brand(String name, String code, String website, String logoUrl) {
        this.name = name;
        this.code = code;
        this.website = website;
        this.logoUrl = logoUrl;
    }

    public static Brand register(String name, String code, String website, String logoUrl) {
        requireName(name);
        requireCode(code);
        return new Brand(name.trim(), normalizeCode(code), blankToNull(website), blankToNull(logoUrl));
    }

    public void rename(String name, String code, String website, String logoUrl) {
        requireName(name);
        requireCode(code);
        this.name = name.trim();
        this.code = normalizeCode(code);
        this.website = blankToNull(website);
        this.logoUrl = blankToNull(logoUrl);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getWebsite() {
        return website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la marca es obligatorio");
        }
    }

    private static void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de la marca es obligatorio");
        }
    }

    private static String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
