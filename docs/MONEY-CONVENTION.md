# Convención Money / Currency (ValeERP)

Aplicable a todos los servicios Java del monorepo. Referencia piloto: inventario de ValeERP.

## Regla

- Precio, costo, impuesto, cuota, total o cualquier monto de negocio usa `com.valeerp.kernel.Money`.
- Prohibido `Double` / `double` / `float` para dinero.
- `BigDecimal` solo como transporte interno o columna embebida; la API de dominio es `Money`.

## Tipos

```java
Currency.PEN                    // política local (primera moneda)
Currency.of("PEN")              // ISO-4217, 3 letras

Money.of(amount, Currency.PEN)
Money.pen(amount)               // atajo local
Money.zero(Currency.PEN)

money.amount()                  // BigDecimal, escala 2, HALF_UP
money.currency()                // Currency
money.currencyCode()            // "PEN"
money.plus / minus / times / includedTax
```

JSON (request/response):

```json
{ "amount": 12.50, "currency": "PEN" }
```

Si `currency` viene vacío en deserialización, se asume `PEN` (solo en este kernel local).

## Persistencia

`Money` es `@Embeddable`. En cada entidad:

```java
@Embedded
@AttributeOverrides({
  @AttributeOverride(name = "amount", column = @Column(name = "unit_cost", precision = 19, scale = 2)),
  @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_cost_currency", length = 3))
})
private Money unitCost;
```

No mezclar monedas en una misma operación aritmética: `plus`/`minus` lanzan `DomainException`.

## Otros servicios

Copiar (no compartir JAR aún) `Currency` + `Money` al kernel local del servicio. Migrar campos `Double` de dinero al embebido en el mismo cambio que toque ese agregado. El frontend no recalcula totales: muestra lo que devuelve el caso de uso.
