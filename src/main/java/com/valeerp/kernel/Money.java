package com.valeerp.kernel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Monto monetario inmutable. Escala fija 2; la moneda viaja con el valor.
 * Persistible como {@code @Embedded} con {@code @AttributeOverride} por campo.
 */
@Embeddable
public final class Money {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Column(precision = 19, scale = SCALE, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    protected Money() {
    }

    private Money(BigDecimal amount, Currency currency) {
        this.amount = amount.setScale(SCALE, ROUNDING);
        this.currencyCode = currency.code();
    }

    @JsonCreator
    public static Money fromJson(
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency) {
        Currency resolved = (currency == null || currency.isBlank()) ? Currency.PEN : Currency.of(currency);
        return of(amount, resolved);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new DomainException("El monto es obligatorio");
        }
        if (currency == null) {
            throw new DomainException("La moneda es obligatoria");
        }
        return new Money(amount, currency);
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return of(amount, Currency.of(currencyCode));
    }

    public static Money of(double amount, Currency currency) {
        return of(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(Currency currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public static Money zero(String currencyCode) {
        return zero(Currency.of(currencyCode));
    }

    public static Money pen(BigDecimal amount) {
        return of(amount, Currency.PEN);
    }

    public static Money pen(double amount) {
        return of(BigDecimal.valueOf(amount), Currency.PEN);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency());
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency());
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new DomainException("La cantidad no puede ser negativa");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency());
    }

    public Money times(BigDecimal factor) {
        if (factor == null) {
            throw new DomainException("El factor es obligatorio");
        }
        return new Money(amount.multiply(factor), currency());
    }

    public Money includedTax(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) {
            throw new DomainException("La tasa de impuesto no es válida");
        }
        BigDecimal divisor = BigDecimal.ONE.add(rate);
        BigDecimal base = amount.divide(divisor, SCALE, ROUNDING);
        return new Money(amount.subtract(base), currency());
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        return compareTo(other) >= 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @JsonProperty("amount")
    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return Currency.of(currencyCode);
    }

    @JsonProperty("currency")
    public String currencyCode() {
        return currencyCode;
    }

    private void requireSameCurrency(Money other) {
        if (other == null || !currencyCode.equals(other.currencyCode)) {
            throw new DomainException("No se pueden operar montos de distinta moneda");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Money money)) {
            return false;
        }
        return amount.compareTo(money.amount) == 0 && currencyCode.equals(money.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currencyCode);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currencyCode;
    }
}
