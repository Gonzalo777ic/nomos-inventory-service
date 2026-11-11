package com.valeerp.catalog;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.math.BigDecimal;

public final class CatalogMoneySupport {

    private CatalogMoneySupport() {
    }

    public static Money requireNonNegative(Money money, String field) {
        if (money == null) {
            throw new DomainException(field + " es obligatorio");
        }
        if (money.isNegative()) {
            throw new DomainException(field + " no puede ser negativo");
        }
        return money;
    }

    public static Money penOrZero(BigDecimal amount) {
        if (amount == null) {
            return Money.zero(Currency.PEN);
        }
        return Money.pen(amount);
    }

    public static Money ofAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            throw new DomainException("El monto es obligatorio");
        }
        Currency currency = currencyCode == null || currencyCode.isBlank()
                ? Currency.PEN
                : Currency.of(currencyCode);
        return Money.of(amount, currency);
    }
}
