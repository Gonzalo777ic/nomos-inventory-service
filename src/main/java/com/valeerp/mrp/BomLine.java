package com.valeerp.mrp;

import com.valeerp.kernel.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Componente de una {@link BillOfMaterials}.
 * {@code childBomId} ancla un sub-BOM multi-nivel; si es nulo, la explosión
 * puede resolver el BOM activo del componente en el caso de uso.
 */
@Entity
@Table(name = "mrp_bom_lines")
public class BomLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterials bom;

    @Column(name = "component_product_id", nullable = false)
    private Long componentProductId;

    /** Cantidad del componente por cada {@link BillOfMaterials#getOutputQuantity()} unidades terminadas. */
    @Column(name = "quantity_per", nullable = false)
    private int quantityPer;

    /** Sub-BOM explícito (multi-nivel). Opcional. */
    @Column(name = "child_bom_id")
    private Long childBomId;

    @Column(length = 240)
    private String notes;

    protected BomLine() {
    }

    static BomLine of(Long componentProductId, int quantityPer, Long childBomId, String notes) {
        if (componentProductId == null) {
            throw new DomainException("El componente de la BOM exige producto");
        }
        if (quantityPer <= 0) {
            throw new DomainException("La cantidad del componente debe ser positiva");
        }
        BomLine line = new BomLine();
        line.componentProductId = componentProductId;
        line.quantityPer = quantityPer;
        line.childBomId = childBomId;
        line.notes = notes;
        return line;
    }

    void attach(BillOfMaterials bom) {
        this.bom = bom;
    }

    public Long getId() {
        return id;
    }

    public Long getComponentProductId() {
        return componentProductId;
    }

    public int getQuantityPer() {
        return quantityPer;
    }

    public Long getChildBomId() {
        return childBomId;
    }

    public String getNotes() {
        return notes;
    }
}
