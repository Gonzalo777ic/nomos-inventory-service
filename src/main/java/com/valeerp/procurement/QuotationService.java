package com.valeerp.procurement;

import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.PurchaseOrder;
import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.model.QuotationDetail;
import com.valeerp.inventory.service.model.QuotationStatus;
import com.valeerp.inventory.service.model.Supplier;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.PurchaseOrderRepository;
import com.valeerp.inventory.service.repository.QuotationRepository;
import com.valeerp.inventory.service.repository.SupplierRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.procurement.QuotationDtos.ConvertResponse;
import com.valeerp.procurement.QuotationDtos.CreateRequest;
import com.valeerp.procurement.QuotationDtos.LineRequest;
import com.valeerp.procurement.QuotationDtos.LineResponse;
import com.valeerp.procurement.QuotationDtos.QuotationResponse;
import com.valeerp.procurement.QuotationDtos.UpdateDraftRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final Clock clock;
    private final ProcurementProperties procurementProperties;

    public QuotationService(QuotationRepository quotationRepository,
                            SupplierRepository supplierRepository,
                            ProductRepository productRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            Clock clock,
                            ProcurementProperties procurementProperties) {
        this.quotationRepository = quotationRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.clock = clock;
        this.procurementProperties = procurementProperties;
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> list(Long supplierId) {
        List<Quotation> quotations = supplierId == null
                ? quotationRepository.findByStatusNot(QuotationStatus.BORRADOR)
                : quotationRepository.findBySupplierId(supplierId);
        return quotations.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuotationResponse get(Long id) {
        return toResponse(requireWithDetails(id));
    }

    @Transactional
    public QuotationResponse create(CreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new DomainException("Proveedor no existe"));
        Quotation quotation = Quotation.draft(
                supplier,
                request.requestDate() == null ? LocalDate.now(clock) : request.requestDate(),
                request.expirationDate(),
                request.notes()
        );
        addLines(quotation, request.lines());
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse updateDraft(Long id, UpdateDraftRequest request) {
        Quotation quotation = requireWithDetails(id);
        quotation.replaceNotes(request.notes());
        quotation.clearLines();
        addLines(quotation, request.lines());
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse send(Long id) {
        Quotation quotation = requireWithDetails(id);
        quotation.send(LocalDate.now(clock));
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse cancel(Long id) {
        Quotation quotation = requireWithDetails(id);
        quotation.cancel();
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse markResponded(Long id) {
        Quotation quotation = requireWithDetails(id);
        quotation.markResponded();
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse accept(Long id) {
        Quotation quotation = requireWithDetails(id);
        quotation.accept();
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse reject(Long id) {
        Quotation quotation = requireWithDetails(id);
        quotation.reject();
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public QuotationResponse linkProduct(Long id, Long detailId, Long productId) {
        Quotation quotation = requireWithDetails(id);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DomainException("Producto no encontrado"));
        quotation.linkLineProduct(detailId, product);
        return toResponse(quotationRepository.save(quotation));
    }

    @Transactional
    public ConvertResponse convert(Long id) {
        Quotation quotation = requireWithDetails(id);
        PurchaseOrder order = quotation.convertToPurchaseOrder(
                procurementProperties.getDefaultLeadDays(), LocalDate.now(clock));
        quotationRepository.save(quotation);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return new ConvertResponse(quotation.getId(), saved.getId(), quotation.getStatus());
    }

    private void addLines(Quotation quotation, List<LineRequest> lines) {
        if (lines == null) {
            return;
        }
        for (LineRequest line : lines) {
            Money price = line.quotedPrice();
            if (line.productId() != null) {
                Product product = productRepository.findById(line.productId())
                        .orElseThrow(() -> new DomainException("Producto ID " + line.productId() + " no existe"));
                quotation.addCatalogLine(
                        product,
                        line.quantity(),
                        price,
                        line.leadTimeDays(),
                        line.promisedDeliveryDate());
            } else {
                quotation.addSuggestedLine(
                        line.productName(),
                        line.skuSuggestion(),
                        line.quantity(),
                        price,
                        line.leadTimeDays(),
                        line.promisedDeliveryDate());
            }
        }
    }

    private Quotation requireWithDetails(Long id) {
        return quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new DomainException("Cotización no encontrada"));
    }

    private QuotationResponse toResponse(Quotation quotation) {
        List<LineResponse> lines = quotation.getDetails().stream()
                .map(this::toLine)
                .toList();
        return new QuotationResponse(
                quotation.getId(),
                quotation.getSupplier().getId(),
                quotation.getSupplier().getName(),
                quotation.getRequestDate(),
                quotation.getExpirationDate(),
                quotation.getStatus(),
                quotation.getTotalEstimated(),
                quotation.getNotes(),
                lines
        );
    }

    private LineResponse toLine(QuotationDetail detail) {
        return new LineResponse(
                detail.getId(),
                detail.getProduct() == null ? null : detail.getProduct().getId(),
                detail.getProductName(),
                detail.getSkuSuggestion(),
                detail.getQuantity(),
                detail.getQuotedPrice(),
                detail.getLeadTimeDays(),
                detail.getPromisedDeliveryDate()
        );
    }
}
