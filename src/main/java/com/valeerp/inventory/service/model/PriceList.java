package com.valeerp.inventory.service.model;

import com.valeerp.catalog.PriceListKind;
import com.valeerp.kernel.Currency;
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
 * Lista de precios de catálogo (estándar / mayorista).
 */
@Entity
@Table(name = "price_lists")
public class PriceList extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceListKind kind;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    protected PriceList() {
    }

    private PriceList(String code, String name, PriceListKind kind, String currencyCode) {
        this.code = code;
        this.name = name;
        this.kind = kind;
        this.currencyCode = currencyCode;
    }

    public static PriceList open(String code, String name, PriceListKind kind, String currencyCode) {
        if (code == null || code.isBlank()) {
            throw new DomainException("El código de la lista es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la lista es obligatorio");
        }
        if (kind == null) {
            throw new DomainException("El tipo de lista es obligatorio");
        }
        Currency currency = currencyCode == null || currencyCode.isBlank()
                ? Currency.PEN
                : Currency.of(currencyCode);
        return new PriceList(code.trim().toUpperCase(), name.trim(), kind, currency.code());
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre de la lista es obligatorio");
        }
        this.name = name.trim();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public PriceListKind getKind() {
        return kind;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Currency currency() {
        return Currency.of(currencyCode);
    }
}
