package com.valeerp.procurement;

import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.repository.QuotationRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.QuotationComparative.ComparisonResult;
import com.valeerp.procurement.QuotationComparative.Offer;
import com.valeerp.procurement.QuotationComparative.ProductComparison;
import com.valeerp.procurement.QuotationComparisonDtos.CompareQuotationsRequest;
import com.valeerp.procurement.QuotationComparisonDtos.ComparisonResponse;
import com.valeerp.procurement.QuotationComparisonDtos.OfferResponse;
import com.valeerp.procurement.QuotationComparisonDtos.ProductComparisonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuotationComparisonService {

    private final QuotationRepository quotationRepository;

    public QuotationComparisonService(QuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    @Transactional(readOnly = true)
    public ComparisonResponse compare(CompareQuotationsRequest request) {
        List<Long> ids = request.quotationIds().stream().distinct().toList();
        if (ids.size() < 2) {
            throw new DomainException("El comparativo exige al menos dos cotizaciones distintas");
        }
        List<Quotation> quotations = quotationRepository.findAllWithDetailsByIdIn(ids);
        Set<Long> found = new HashSet<>();
        for (Quotation quotation : quotations) {
            found.add(quotation.getId());
        }
        for (Long id : ids) {
            if (!found.contains(id)) {
                throw new DomainException("Cotización no encontrada: " + id);
            }
        }
        return toResponse(QuotationComparative.compare(quotations));
    }

    private ComparisonResponse toResponse(ComparisonResult result) {
        List<ProductComparisonResponse> products = result.products().stream()
                .map(this::toProduct)
                .toList();
        return new ComparisonResponse(result.quotationIds(), products);
    }

    private ProductComparisonResponse toProduct(ProductComparison comparison) {
        List<OfferResponse> offers = comparison.offers().stream()
                .map(this::toOffer)
                .toList();
        return new ProductComparisonResponse(
                comparison.productId(),
                comparison.productName(),
                offers,
                comparison.bestQuotationId(),
                comparison.bestSupplierId(),
                comparison.bestUnitPrice());
    }

    private OfferResponse toOffer(Offer offer) {
        return new OfferResponse(
                offer.quotationId(),
                offer.supplierId(),
                offer.supplierName(),
                offer.quantity(),
                offer.unitPrice());
    }
}
