package com.valeerp.procurement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalVendorBillAccountingStub implements VendorBillAccountingPort {

    private static final Logger log = LoggerFactory.getLogger(LocalVendorBillAccountingStub.class);

    @Override
    public String proposeEntry(VendorBill bill) {
        String ref = "VB-STUB-" + bill.getId() + "-" + bill.getDocumentNumber();
        log.info("Asiento stub interno propuesto para factura proveedor {}: total={}",
                bill.getDocumentNumber(), bill.getTotalAmount());
        return ref;
    }
}
