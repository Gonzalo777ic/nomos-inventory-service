package com.valeerp.procurement;

import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.PurchaseOrder;
import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.model.QuotationStatus;
import com.valeerp.inventory.service.model.Supplier;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuotationStateMachineTest {

    @Test
    void draftSendRespondAcceptConvert() {
        Quotation quotation = Quotation.draft(supplier(), LocalDate.of(2026, 11, 10), null, "RFQ demo");
        Product product = product("SKU-1");
        quotation.addCatalogLine(product, 2, Money.pen(new BigDecimal("15.50")));

        assertEquals(QuotationStatus.BORRADOR, quotation.getStatus());
        assertEquals(Money.pen(new BigDecimal("31.00")), quotation.getTotalEstimated());

        quotation.send(LocalDate.of(2026, 11, 11));
        assertEquals(QuotationStatus.ENVIADO, quotation.getStatus());

        quotation.markResponded();
        quotation.accept();
        PurchaseOrder order = quotation.convertToPurchaseOrder(7, LocalDate.of(2026, 11, 12));

        assertEquals(QuotationStatus.CONVERTIDO, quotation.getStatus());
        assertEquals(Money.pen(new BigDecimal("31.00")), order.getTotalAmount());
    }

    @Test
    void cancelIsViaStateNotPhysicalDelete() {
        Quotation quotation = Quotation.draft(supplier(), LocalDate.now(), null, null);
        quotation.addSuggestedLine("Producto nuevo", "NEW-1", 1, Money.pen(10));
        quotation.send(LocalDate.now());
        quotation.cancel();
        assertEquals(QuotationStatus.CANCELADO, quotation.getStatus());
        assertEquals(1, quotation.getDetails().size());
    }

    @Test
    void invalidTransitionIsRejected() {
        Quotation quotation = Quotation.draft(supplier(), LocalDate.now(), null, null);
        assertThrows(DomainException.class, quotation::accept);
    }

    private static Supplier supplier() {
        Supplier supplier = new Supplier();
        supplier.setName("Proveedor Demo");
        supplier.setTaxId("20123456789");
        return supplier;
    }

    private static Product product(String sku) {
        return Product.register(
                sku,
                "Producto " + sku,
                1L, 1L, 1L,
                null,
                Money.pen(new BigDecimal("20.00")),
                2,
                CostingMethod.STANDARD,
                Money.pen(new BigDecimal("8.00"))
        );
    }
}
