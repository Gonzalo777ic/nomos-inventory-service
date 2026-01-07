package com.nomos.inventory.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quotation_details")
@Data
public class QuotationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    @JsonIgnore
    private Quotation quotation;

    /**
     * ESTRATEGIA HÍBRIDA:
     * - Si product_id tiene valor: Es un producto de nuestro catálogo.
     * - Si product_id es NULL: Es una sugerencia nueva del proveedor.
     */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    /**
     * Nombre del producto.
     * - Si hay 'product', copiamos su nombre aquí para referencia histórica.
     * - Si NO hay 'product', aquí va el texto libre del proveedor (Ej: "Nuevo Modelo 2026").
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "quoted_price")
    private Double quotedPrice;

    @Column(name = "sku_suggestion")
    private String skuSuggestion; 
}