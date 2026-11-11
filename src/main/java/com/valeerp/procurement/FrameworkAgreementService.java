package com.valeerp.procurement;

import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.procurement.FrameworkAgreementDtos.CreateRequest;
import com.valeerp.procurement.FrameworkAgreementDtos.LineRequest;
import com.valeerp.procurement.FrameworkAgreementDtos.LineResponse;
import com.valeerp.procurement.FrameworkAgreementDtos.PriceLookupResponse;
import com.valeerp.procurement.FrameworkAgreementDtos.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FrameworkAgreementService {

    private final FrameworkAgreementRepository agreementRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public FrameworkAgreementService(FrameworkAgreementRepository agreementRepository,
                                     SupplierRepository supplierRepository,
                                     ProductRepository productRepository,
                                     Clock clock) {
        this.agreementRepository = agreementRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Response> listBySupplier(Long supplierId) {
        return agreementRepository.findBySupplierIdOrderByValidFromDesc(supplierId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Response get(Long id) {
        return toResponse(requireWithLines(id));
    }

    @Transactional
    public Response create(CreateRequest request) {
        if (!supplierRepository.existsById(request.supplierId())) {
            throw new DomainException("Proveedor no encontrado");
        }
        String code = request.code().trim().toUpperCase();
        if (agreementRepository.existsByCode(code)) {
            throw new DomainException("Ya existe un acuerdo con código " + code);
        }
        PurchaseFrameworkAgreement agreement = PurchaseFrameworkAgreement.draft(
                code, request.supplierId(), request.validFrom(), request.validTo(), request.notes());
        for (LineRequest line : request.lines()) {
            if (!productRepository.existsById(line.productId())) {
                throw new DomainException("Producto no encontrado: " + line.productId());
            }
            agreement.addPrice(line.productId(), line.unitPrice(), line.priceValidFrom(), line.priceValidTo());
        }
        return toResponse(agreementRepository.save(agreement));
    }

    @Transactional
    public Response activate(Long id) {
        PurchaseFrameworkAgreement agreement = requireWithLines(id);
        agreement.activate();
        return toResponse(agreementRepository.save(agreement));
    }

    @Transactional
    public Response close(Long id) {
        PurchaseFrameworkAgreement agreement = requireWithLines(id);
        agreement.close();
        return toResponse(agreementRepository.save(agreement));
    }

    @Transactional
    public void retire(Long id) {
        PurchaseFrameworkAgreement agreement = requireWithLines(id);
        if (agreement.getStatus() == FrameworkAgreementStatus.ACTIVE) {
            agreement.close();
        }
        agreement.retire(Actors.current(), Instant.now(clock));
        agreementRepository.save(agreement);
    }

    @Transactional(readOnly = true)
    public Optional<PriceLookupResponse> lookupPrice(Long supplierId, Long productId, LocalDate onDate) {
        LocalDate date = onDate == null ? LocalDate.now(clock) : onDate;
        for (PurchaseFrameworkAgreement agreement : agreementRepository.findActiveBySupplierId(supplierId)) {
            Optional<Money> price = agreement.priceOn(productId, date);
            if (price.isPresent()) {
                return Optional.of(new PriceLookupResponse(
                        agreement.getId(),
                        agreement.getCode(),
                        productId,
                        date,
                        price.get()
                ));
            }
        }
        return Optional.empty();
    }

    private PurchaseFrameworkAgreement requireWithLines(Long id) {
        return agreementRepository.findByIdWithLines(id)
                .orElseThrow(() -> new DomainException("Acuerdo marco no encontrado"));
    }

    private Response toResponse(PurchaseFrameworkAgreement agreement) {
        List<LineResponse> lines = agreement.getLines().stream()
                .map(line -> new LineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getUnitPrice(),
                        line.getPriceValidFrom(),
                        line.getPriceValidTo()
                ))
                .toList();
        return new Response(
                agreement.getId(),
                agreement.getCode(),
                agreement.getSupplierId(),
                agreement.getValidFrom(),
                agreement.getValidTo(),
                agreement.getStatus(),
                agreement.getNotes(),
                lines
        );
    }
}
