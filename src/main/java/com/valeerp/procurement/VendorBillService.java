package com.valeerp.procurement;

import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import com.valeerp.procurement.VendorBillDtos.CreateVendorBillRequest;
import com.valeerp.procurement.VendorBillDtos.LineRequest;
import com.valeerp.procurement.VendorBillDtos.VendorBillLineResponse;
import com.valeerp.procurement.VendorBillDtos.VendorBillResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorBillService {

    private final VendorBillRepository vendorBillRepository;
    private final VendorBillAccountingPort accountingPort;

    public VendorBillService(VendorBillRepository vendorBillRepository,
                             VendorBillAccountingPort accountingPort) {
        this.vendorBillRepository = vendorBillRepository;
        this.accountingPort = accountingPort;
    }

    @Transactional(readOnly = true)
    public List<VendorBillResponse> list() {
        return vendorBillRepository.findAllWithLines().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VendorBillResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public VendorBillResponse create(CreateVendorBillRequest request) {
        VendorBill bill = VendorBill.draft(
                request.supplierId(),
                request.documentNumber(),
                request.billDate(),
                request.purchaseOrderId());
        for (LineRequest line : request.lines()) {
            bill.addLine(line.productId(), line.description(), line.quantity(), line.unitPrice());
        }
        Money tax = request.taxAmount() == null ? Money.zero(Currency.PEN) : request.taxAmount();
        bill.applyTax(tax);
        return toResponse(vendorBillRepository.save(bill));
    }

    @Transactional
    public VendorBillResponse post(Long id) {
        VendorBill bill = require(id);
        // Persistir primero para que el stub reciba id asignado.
        vendorBillRepository.save(bill);
        bill.post(accountingPort);
        return toResponse(vendorBillRepository.save(bill));
    }

    @Transactional
    public VendorBillResponse markPaid(Long id) {
        VendorBill bill = require(id);
        bill.markPaid();
        return toResponse(vendorBillRepository.save(bill));
    }

    @Transactional
    public VendorBillResponse cancel(Long id) {
        VendorBill bill = require(id);
        bill.cancel();
        return toResponse(vendorBillRepository.save(bill));
    }

    private VendorBill require(Long id) {
        return vendorBillRepository.findByIdWithLines(id)
                .orElseThrow(() -> new DomainException("Factura de proveedor no encontrada"));
    }

    private VendorBillResponse toResponse(VendorBill bill) {
        List<VendorBillLineResponse> lines = bill.getLines().stream()
                .map(line -> new VendorBillLineResponse(
                        line.getId(),
                        line.getProductId(),
                        line.getDescription(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.lineTotal()))
                .toList();
        return new VendorBillResponse(
                bill.getId(),
                bill.getSupplierId(),
                bill.getPurchaseOrderId(),
                bill.getDocumentNumber(),
                bill.getBillDate(),
                bill.getStatus().name(),
                bill.getUntaxedAmount(),
                bill.getTaxAmount(),
                bill.getTotalAmount(),
                bill.getAccountingStubRef(),
                lines);
    }
}
