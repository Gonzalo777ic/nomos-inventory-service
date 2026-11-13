package com.valeerp.procurement;

import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.model.QuotationDetail;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Comparativo de cotizaciones por producto/precio (dominio puro, sin ventas ni contabilidad).
 */
public final class QuotationComparative {

    private QuotationComparative() {
    }

    public static ComparisonResult compare(List<Quotation> quotations) {
        if (quotations == null || quotations.size() < 2) {
            throw new DomainException("El comparativo exige al menos dos cotizaciones");
        }
        Map<String, ProductBucket> byProduct = new LinkedHashMap<>();
        for (Quotation quotation : quotations) {
            if (quotation.getDetails() == null) {
                continue;
            }
            for (QuotationDetail detail : quotation.getDetails()) {
                String key = productKey(detail);
                ProductBucket bucket = byProduct.computeIfAbsent(key, ignored -> new ProductBucket(
                        detail.getProduct() == null ? null : detail.getProduct().getId(),
                        detail.getProductName()));
                Money price = detail.getQuotedPrice() == null
                        ? Money.zero(Currency.PEN)
                        : detail.getQuotedPrice();
                bucket.offers.add(new Offer(
                        quotation.getId(),
                        quotation.getSupplier() == null ? null : quotation.getSupplier().getId(),
                        quotation.getSupplier() == null ? null : quotation.getSupplier().getName(),
                        detail.getQuantity(),
                        price));
            }
        }
        List<ProductComparison> products = new ArrayList<>();
        for (ProductBucket bucket : byProduct.values()) {
            List<Offer> ranked = bucket.offers.stream()
                    .sorted(Comparator.comparing((Offer o) -> o.unitPrice().amount())
                            .thenComparing(o -> o.quotationId() == null ? Long.MAX_VALUE : o.quotationId()))
                    .toList();
            Offer best = ranked.isEmpty() ? null : ranked.get(0);
            products.add(new ProductComparison(
                    bucket.productId,
                    bucket.productName,
                    ranked,
                    best == null ? null : best.quotationId(),
                    best == null ? null : best.supplierId(),
                    best == null ? null : best.unitPrice()));
        }
        return new ComparisonResult(
                quotations.stream().map(Quotation::getId).filter(Objects::nonNull).toList(),
                products);
    }

    private static String productKey(QuotationDetail detail) {
        if (detail.getProduct() != null && detail.getProduct().getId() != null) {
            return "P:" + detail.getProduct().getId();
        }
        String name = detail.getProductName() == null ? "" : detail.getProductName().trim().toLowerCase();
        return "N:" + name;
    }

    private static final class ProductBucket {
        private final Long productId;
        private final String productName;
        private final List<Offer> offers = new ArrayList<>();

        private ProductBucket(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }
    }

    public record Offer(
            Long quotationId,
            Long supplierId,
            String supplierName,
            Integer quantity,
            Money unitPrice
    ) {
    }

    public record ProductComparison(
            Long productId,
            String productName,
            List<Offer> offers,
            Long bestQuotationId,
            Long bestSupplierId,
            Money bestUnitPrice
    ) {
    }

    public record ComparisonResult(
            List<Long> quotationIds,
            List<ProductComparison> products
    ) {
    }
}
