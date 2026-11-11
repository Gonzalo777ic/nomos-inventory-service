package com.nomos.inventory.service.model;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Categoría jerárquica de catálogo. {@code parentId} es referencia local (sin grafo JPA).
 * Unicidad vigente de nombre:
 * {@code CREATE UNIQUE INDEX uq_categories_name_active ON categories (name) WHERE deleted_at IS NULL;}
 */
@Entity
@Table(name = "categories")
public class Category extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    protected Category() {
    }

    private Category(String name, String description, Long parentId) {
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    public static Category create(String name, String description, Long parentId) {
        requireName(name);
        return new Category(name.trim(), blankToNull(description), parentId);
    }

    public void rename(String name, String description) {
        requireName(name);
        this.name = name.trim();
        this.description = blankToNull(description);
    }

    public void moveUnder(Long parentId) {
        if (parentId != null && parentId.equals(this.id)) {
            throw new DomainException("Una categoría no puede ser padre de sí misma");
        }
        this.parentId = parentId;
    }

    public boolean isRoot() {
        return parentId == null;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getParentId() {
        return parentId;
    }

    private static void requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la categoría es obligatorio");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
