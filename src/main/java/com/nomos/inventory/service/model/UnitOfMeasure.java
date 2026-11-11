package com.nomos.inventory.service.model;

import com.valeerp.catalog.UomCategory;
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

/**
 * Unidad de medida del catálogo (maestro con soft-delete).
 * <p>
 * Unicidad de abreviatura entre vigentes (PostgreSQL; aplicar a mano — no hay Flyway):
 * {@code CREATE UNIQUE INDEX IF NOT EXISTS uq_uom_abbreviation_active
 * ON unit_of_measures (abbreviation) WHERE deleted_at IS NULL;}
 * Ver {@code src/main/resources/db/catalog-partial-indexes.sql}.
 */
@Entity
@Table(name = "unit_of_measures")
public class UnitOfMeasure extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String abbreviation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UomCategory category;

    protected UnitOfMeasure() {
    }

    private UnitOfMeasure(String name, String abbreviation, UomCategory category) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.category = category;
    }

    public static UnitOfMeasure define(String name, String abbreviation, UomCategory category) {
        requireName(name);
        requireAbbreviation(abbreviation);
        if (category == null) {
            throw new DomainException("La categoría de UoM es obligatoria");
        }
        return new UnitOfMeasure(name.trim(), normalizeAbbreviation(abbreviation), category);
    }

    public void redefine(String name, String abbreviation, UomCategory category) {
        requireName(name);
        requireAbbreviation(abbreviation);
        if (category == null) {
            throw new DomainException("La categoría de UoM es obligatoria");
        }
        this.name = name.trim();
        this.abbreviation = normalizeAbbreviation(abbreviation);
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public UomCategory getCategory() {
        return category;
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la unidad de medida es obligatorio");
        }
    }

    private static void requireAbbreviation(String abbreviation) {
        if (abbreviation == null || abbreviation.isBlank()) {
            throw new DomainException("La abreviatura de la unidad de medida es obligatoria");
        }
    }

    private static String normalizeAbbreviation(String abbreviation) {
        return abbreviation.trim().toUpperCase();
    }
}
