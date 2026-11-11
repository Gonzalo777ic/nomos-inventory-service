package com.valeerp.mrp;

import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.SoftDeletable;
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
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista de materiales multi-nivel (maestro). Soft-delete vía {@link SoftDeletable};
 * las líneas hijas no se borran en cascada física al dar de baja la raíz.
 */
@Entity
@Table(name = "mrp_bills_of_materials")
public class BillOfMaterials extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    /** Producto terminado que fabrica esta BOM. */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Unidades terminadas que produce un ciclo de esta BOM (base de quantityPer). */
    @Column(name = "output_quantity", nullable = false)
    private int outputQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BomStatus status;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BomLine> lines = new ArrayList<>();

    protected BillOfMaterials() {
    }

    public static BillOfMaterials draft(String code, Long productId, int outputQuantity, String notes) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de la BOM es obligatorio");
        }
        if (productId == null) {
            throw new DomainException("La BOM exige producto terminado");
        }
        if (outputQuantity <= 0) {
            throw new DomainException("La cantidad de salida de la BOM debe ser positiva");
        }
        BillOfMaterials bom = new BillOfMaterials();
        bom.code = code.trim().toUpperCase();
        bom.productId = productId;
        bom.outputQuantity = outputQuantity;
        bom.notes = notes;
        bom.status = BomStatus.DRAFT;
        return bom;
    }

    public void addComponent(Long componentProductId, int quantityPer, Long childBomId, String notes) {
        assertEditable();
        if (productId.equals(componentProductId)) {
            throw new DomainException("La BOM no puede listarse a sí misma como componente");
        }
        BomLine line = BomLine.of(componentProductId, quantityPer, childBomId, notes);
        line.attach(this);
        lines.add(line);
    }

    public void activate() {
        if (status != BomStatus.DRAFT && status != BomStatus.ARCHIVED) {
            throw new DomainException("Solo se activa una BOM en borrador o archivada");
        }
        if (lines.isEmpty()) {
            throw new DomainException("La BOM no tiene componentes");
        }
        this.status = BomStatus.ACTIVE;
    }

    public void archive() {
        if (status != BomStatus.ACTIVE) {
            throw new DomainException("Solo se archiva una BOM activa");
        }
        this.status = BomStatus.ARCHIVED;
    }

    /**
     * Consumo teórico de un nivel (sin expandir sub-BOM).
     * {@code finishedQuantity} es la cantidad a fabricar del producto terminado.
     */
    public List<TheoreticalNeed> theoreticalOneLevel(int finishedQuantity) {
        if (finishedQuantity <= 0) {
            throw new DomainException("La cantidad a fabricar debe ser positiva");
        }
        if (status != BomStatus.ACTIVE) {
            throw new DomainException("Solo una BOM activa calcula consumo teórico");
        }
        List<TheoreticalNeed> needs = new ArrayList<>();
        for (BomLine line : lines) {
            int required = scaledQuantity(line.getQuantityPer(), finishedQuantity);
            needs.add(new TheoreticalNeed(
                    line.getComponentProductId(),
                    required,
                    line.getChildBomId()
            ));
        }
        return List.copyOf(needs);
    }

    private int scaledQuantity(int quantityPer, int finishedQuantity) {
        long raw = (long) quantityPer * (long) finishedQuantity;
        if (raw % outputQuantity != 0) {
            throw new DomainException(
                    "La cantidad a fabricar no es múltiplo de la salida de la BOM (" + outputQuantity + ")");
        }
        long result = raw / outputQuantity;
        if (result > Integer.MAX_VALUE) {
            throw new DomainException("Cantidad teórica fuera de rango");
        }
        return (int) result;
    }

    private void assertEditable() {
        if (status != BomStatus.DRAFT) {
            throw new DomainException("Solo se editan componentes en borrador");
        }
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Long getProductId() {
        return productId;
    }

    public int getOutputQuantity() {
        return outputQuantity;
    }

    public BomStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public List<BomLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Necesidad teórica de un componente (un nivel). {@code childBomId} puede ser nulo.
     */
    public record TheoreticalNeed(Long componentProductId, int quantity, Long childBomId) {
    }
}
